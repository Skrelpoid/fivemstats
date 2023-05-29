package de.skrelpoid.fivemstats.views.players;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import de.skrelpoid.fivemstats.data.entity.Player;
import de.skrelpoid.fivemstats.data.service.PlayerService;
import de.skrelpoid.fivemstats.views.MainLayout;
import jakarta.annotation.security.PermitAll;

@PageTitle("Players")
@Route(value = "players/:playerId?/:action?(edit)", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class PlayersView extends Div implements BeforeEnterObserver {

	private static final long serialVersionUID = 1L;
	private static final String PLAYER_ID = "playerId";
    private static final String PLAYER_EDIT_ROUTE_TEMPLATE = "players/%s/edit";

    private final Grid<Player> grid = new Grid<>(Player.class, false);

    private TextField discordId;
    private TextField name;
    private TextField discordIdentifier;
    private TextField alias1;
    private TextField alias2;
    private TextField alias3;
    private TextField steamId;
    private TextField license;
    private TextField license2;
    private TextField xboxLiveId;
    private TextField liveId;
    private TextField fivemId;
    private TextField longTermSecondsLogged;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<Player> binder;

    private Player player;

    private final PlayerService playerService;

    public PlayersView(final PlayerService playerService) {
        this.playerService = playerService;
        addClassNames("players-view");

        // Create UI
        final SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("discordId").setAutoWidth(true);
        grid.addColumn("name").setAutoWidth(true);
        grid.addColumn("discordIdentifier").setAutoWidth(true);
        grid.addColumn("alias1").setAutoWidth(true);
        grid.addColumn("alias2").setAutoWidth(true);
        grid.addColumn("alias3").setAutoWidth(true);
        grid.addColumn("steamId").setAutoWidth(true);
        grid.addColumn("license").setAutoWidth(true);
        grid.addColumn("license2").setAutoWidth(true);
        grid.addColumn("xboxLiveId").setAutoWidth(true);
        grid.addColumn("liveId").setAutoWidth(true);
        grid.addColumn("fivemId").setAutoWidth(true);
        grid.addColumn("longTermSecondsLogged").setAutoWidth(true);

        grid.setItems(query -> playerService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(PLAYER_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(PlayersView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Player.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.player == null) {
                    this.player = new Player();
                }
                binder.writeBean(this.player);
                playerService.update(this.player);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(PlayersView.class);
            } catch (final ObjectOptimisticLockingFailureException exception) {
                final Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (final ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        final Optional<Long> playerId = event.getRouteParameters().get(PLAYER_ID).map(Long::parseLong);
        if (playerId.isPresent()) {
            final Optional<Player> playerFromBackend = playerService.get(playerId.get());
            if (playerFromBackend.isPresent()) {
                populateForm(playerFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested player was not found, ID = %s", playerId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(PlayersView.class);
            }
        }
    }

    private void createEditorLayout(final SplitLayout splitLayout) {
        final Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        final Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        final FormLayout formLayout = new FormLayout();
        discordId              = new TextField("Discord ID");
        discordId.setReadOnly(true);
        name                   = new TextField("Name");
        name.setReadOnly(true);
        discordIdentifier      = new TextField("Discord Identifier");
        alias1                 = new TextField("Alias 1");
        alias2                 = new TextField("Alias 2");
        alias3                 = new TextField("Alias 3");
        steamId                = new TextField("Steam ID");
        steamId.setReadOnly(true);
        license                = new TextField("License");
        license.setReadOnly(true);
        license2               = new TextField("License2");
        license2.setReadOnly(true);
        xboxLiveId             = new TextField("Xbox Live ID");
        xboxLiveId.setReadOnly(true);
        liveId                 = new TextField("Live ID");
        liveId.setReadOnly(true);
        fivemId                = new TextField("FiveM ID");
        fivemId.setReadOnly(true);
        longTermSecondsLogged  = new TextField("Total Time Logged");
        longTermSecondsLogged.setReadOnly(true);
        
        formLayout.add(discordId,           
        		name,                
        		discordIdentifier,   
        		alias1,              
        		alias2,              
        		alias3,              
        		steamId,             
        		license,             
        		license2,            
        		xboxLiveId,          
        		liveId,              
        		fivemId,             
        		longTermSecondsLogged);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(final Div editorLayoutDiv) {
        final HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(final SplitLayout splitLayout) {
        final Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(final Player value) {
        this.player = value;
        binder.readBean(this.player);

    }
}
