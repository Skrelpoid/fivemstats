package de.skrelpoid.fivemstats.views.groups;

import static java.util.stream.Collectors.joining;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIcon;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
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
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import de.skrelpoid.fivemstats.data.entity.Group;
import de.skrelpoid.fivemstats.data.entity.Player;
import de.skrelpoid.fivemstats.data.service.GroupService;
import de.skrelpoid.fivemstats.data.service.PlayerService;
import de.skrelpoid.fivemstats.views.MainLayout;
import jakarta.annotation.security.PermitAll;

@PageTitle("Groups")
@Route(value = "groups/:groupId?/:action?(edit)", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class GroupsView extends Div implements BeforeEnterObserver {

	private static final long serialVersionUID = 1L;
	private static final String GROUP_ID = "groupId";
	private static final String GROUP_EDIT_ROUTE_TEMPLATE = "groups/%s/edit";

	private final Grid<Group> grid = new Grid<>(Group.class, false);

	private TextField id;
	private TextField name;
	private MultiSelectComboBox<Player> players;

	private final Button cancel = new Button("Cancel");
	private final Button save = new Button("Save");
	private final Button delete = new Button(LineAwesomeIcon.TRASH_SOLID.create());

	private final BeanValidationBinder<Group> binder;

	private Group group;

	private final GroupService groupService;
	private final PlayerService playerService;

	public GroupsView(final GroupService groupService, final PlayerService playerService) {
		this.groupService = groupService;
		this.playerService = playerService;
		addClassNames("groups-view");

		// Create UI
		final SplitLayout splitLayout = new SplitLayout();

		createGridLayout(splitLayout);
		createEditorLayout(splitLayout);

		add(splitLayout);

		// Configure Grid
		grid.addColumn("id").setAutoWidth(true);
		grid.addColumn("name").setAutoWidth(true);
		grid.addColumn(new ComponentRenderer<>(g -> {
			final MultiSelectComboBox<Player> combo = new MultiSelectComboBox<>();
			final List<Player> sorted = g.getPlayers().stream().sorted(Comparator.comparing(Player::toString)).toList();
			combo.setItems(sorted);
			combo.select(sorted);
			combo.setWidthFull();
			combo.setReadOnly(true);
			return combo;
		})).setWidth("75%")
				.setHeader("Players in Group");

		grid.setItems(query -> groupService.list(
				PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
				.stream());
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

		// when a row is selected or deselected, populate form
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				UI.getCurrent().navigate(String.format(GROUP_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
			} else {
				clearForm();
				UI.getCurrent().navigate(GroupsView.class);
			}
		});

		// Configure Form
		binder = new BeanValidationBinder<>(Group.class);

		// Bind fields. This is where you'd define e.g. validation rules

		binder.forMemberField(name);
		binder.forMemberField(players);

		binder.bindInstanceFields(this);

		cancel.addClickListener(e -> {
			clearForm();
			refreshGrid();
		});

		delete.setTooltipText("Hold Down Shift Key while Pressing Delete to Confirm Deletion");
		delete.addClickListener(e -> {
			if (!e.isShiftKey()) {
				Notification.show("Hold Down Shift Key while Pressing Delete to Confirm Deletion", 3000,
						Position.MIDDLE);
				return;
			}
			if (this.group != null) {
				groupService.delete(this.group.getId());
				clearForm();
				refreshGrid();
			}
		});

		save.addClickListener(e -> {
			try {
				if (this.group == null) {
					this.group = new Group();
				}
				binder.writeBean(this.group);
				groupService.update(this.group);
				clearForm();
				refreshGrid();
				Notification.show("Data updated");
				UI.getCurrent().navigate(GroupsView.class);
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
		final Optional<Long> groupId = event.getRouteParameters().get(GROUP_ID).map(Long::parseLong);
		if (groupId.isPresent()) {
			final Optional<Group> groupFromBackend = groupService.get(groupId.get());
			if (groupFromBackend.isPresent()) {
				populateForm(groupFromBackend.get());
			} else {
				Notification.show(
						String.format("The requested group was not found, ID = %s", groupId.get()), 3000,
						Notification.Position.BOTTOM_START);
				// when a row is selected but the data is no longer available,
				// refresh grid
				refreshGrid();
				event.forwardTo(GroupsView.class);
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
		id = new TextField("ID");
		id.setReadOnly(true);
		name = new TextField("Name");
		players = new MultiSelectComboBox<>("Players");
		players.setItems(playerService.findAllAndSortByCanonicalName());
		players.addCustomValueSetListener(event -> {
			final String customValue = event.getDetail();
			final Specification<Player> spec = (r, q, s) -> playerService.buildSearchPredicate(customValue, r, s);
			final List<Player> foundPlayers = playerService.list(spec);
			if (foundPlayers.isEmpty()) {
				// show notification
				final Notification notification =
						Notification.show("No players found for searched value: " + customValue, 3000, Position.MIDDLE);
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			} else {
				players.select(foundPlayers);
				final String foundStr = foundPlayers.stream().map(Player::toString).collect(joining(", "));
				final Notification notification =
						Notification.show("Added players: " + foundStr, 3000, Position.BOTTOM_START);
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			}
		});

		formLayout.add(id, name, players);

		editorDiv.add(formLayout);
		createButtonLayout(editorLayoutDiv);

		splitLayout.addToSecondary(editorLayoutDiv);
	}

	private void createButtonLayout(final Div editorLayoutDiv) {
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setClassName("button-layout");
		cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		delete.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
		buttonLayout.add(save, delete, cancel);
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

	private void populateForm(final Group value) {
		this.group = value;
		binder.readBean(this.group);

	}
}
