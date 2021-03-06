package org.jboss.as.console.client.domain.overview;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.*;
import org.jboss.as.console.client.core.NameTokens;
import org.jboss.as.console.client.core.SuspendableView;
import org.jboss.as.console.client.domain.events.StaleModelEvent;
import org.jboss.as.console.client.domain.model.*;
import org.jboss.as.console.client.domain.profiles.ProfileMgmtPresenter;
import org.jboss.as.console.client.shared.model.DeploymentRecord;
import org.jboss.as.console.client.shared.model.DeploymentStore;

import java.util.List;


/**
 * @author Heiko Braun
 * @date 2/11/11
 */
public class DomainOverviewPresenter
        extends Presenter<DomainOverviewPresenter.MyView, DomainOverviewPresenter.MyProxy>
        implements StaleModelEvent.StaleModelListener {

    private final PlaceManager placeManager;
    private ProfileStore profileStore;
    private ServerGroupStore serverGroupStore;
    private DeploymentStore deploymentStore;

    @ProxyCodeSplit
    @NameToken(NameTokens.ProfileOverviewPresenter)
    public interface MyProxy extends Proxy<DomainOverviewPresenter>, Place {
    }

    public interface MyView extends SuspendableView {
        void setPresenter(DomainOverviewPresenter presenter);
        void updateProfiles(List<ProfileRecord> profiles);
        void updateGroups(List<ServerGroupRecord> groups);
        void updateDeployments(List<DeploymentRecord> deploymentRecords);
    }

    @Inject
    public DomainOverviewPresenter(
            EventBus eventBus, MyView view, MyProxy proxy,
            PlaceManager placeManager, ProfileStore profileStore,
            ServerGroupStore serverGroupStore,
            DeploymentStore deploymentStore) {

        super(eventBus, view, proxy);
        this.placeManager = placeManager;
        this.profileStore = profileStore;
        this.serverGroupStore = serverGroupStore;
        this.deploymentStore = deploymentStore;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        String profileName = request.getParameter("name", "none");
        Log.debug("requested profile: "+ profileName);

    }

    @Override
    protected void onReset() {

        profileStore.loadProfiles(new SimpleCallback<List<ProfileRecord>>() {
            @Override
            public void onSuccess(List<ProfileRecord> result) {
                getView().updateProfiles(result);
            }
        });

        refreshGroups();

        // TODO: this needs to reference a server group
        deploymentStore.loadDeployments(new SimpleCallback<List<DeploymentRecord>>() {
            @Override
            public void onSuccess(List<DeploymentRecord> result) {
                getView().updateDeployments(result);
            }
        });

    }

    private void refreshGroups() {
        serverGroupStore.loadServerGroups(new SimpleCallback<List<ServerGroupRecord>>() {
            @Override
            public void onSuccess(List<ServerGroupRecord> result) {
                getView().updateGroups(result);
            }
        });
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(getEventBus(), ProfileMgmtPresenter.TYPE_MainContent, this);
    }

    // --------------------------------

    @Override
    public void onStaleModel(String modelName) {
        if(modelName.equals(StaleModelEvent.SERVER_GROUPS))
        {
            refreshGroups();
        }
    }
}
