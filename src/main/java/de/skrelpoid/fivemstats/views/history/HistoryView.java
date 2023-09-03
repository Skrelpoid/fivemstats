package de.skrelpoid.fivemstats.views.history;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.skrelpoid.fivemstats.data.entity.PlayerLog;
import de.skrelpoid.fivemstats.data.service.GroupService;
import de.skrelpoid.fivemstats.data.service.PlayerLogService;
import de.skrelpoid.fivemstats.data.service.PlayerService;
import de.skrelpoid.fivemstats.views.MainLayout;
import de.skrelpoid.fivemstats.views.components.Filters;
import jakarta.annotation.security.PermitAll;

@PageTitle("History")
@Route(value = "history", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class HistoryView extends Div {
	private static final long serialVersionUID = 1L;

	private Grid<PlayerLog> grid;

	private final Filters<PlayerLog> filters;
	private final PlayerLogService playerLogService;
	private final GroupService groupService;

	public HistoryView(final PlayerLogService playerLogService, final PlayerService playerService,
			GroupService groupService) {
		this.playerLogService = playerLogService;
		this.groupService = groupService;
		setSizeFull();
		addClassNames("history-view");

		filters = new Filters<>(this::refreshGrid, playerService, groupService, PlayerLog.class);
		final VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
		layout.setSizeFull();
		layout.setPadding(false);
		layout.setSpacing(false);
		add(layout);
	}

	private HorizontalLayout createMobileFilters() {
		// Mobile version
		final HorizontalLayout mobileFilters = new HorizontalLayout();
		mobileFilters.setWidthFull();
		mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
				LumoUtility.AlignItems.CENTER);
		mobileFilters.addClassName("mobile-filters");

		final Icon mobileIcon = new Icon("lumo", "plus");
		final Span filtersHeading = new Span("Filters");
		mobileFilters.add(mobileIcon, filtersHeading);
		mobileFilters.setFlexGrow(1, filtersHeading);
		mobileFilters.addClickListener(e -> {
			if (filters.getClassNames().contains("visible")) {
				filters.removeClassName("visible");
				mobileIcon.getElement().setAttribute("icon", "lumo:plus");
			} else {
				filters.addClassName("visible");
				mobileIcon.getElement().setAttribute("icon", "lumo:minus");
			}
		});
		return mobileFilters;
	}

	private Component createGrid() {
		grid = new Grid<>(PlayerLog.class, false);
		grid.addColumn("player.name").setAutoWidth(true);
		grid.addColumn("player.discordId").setAutoWidth(true);
		grid.addColumn("player.discordIdentifier").setAutoWidth(true);
		grid.addColumn("player.aliases").setAutoWidth(true);
		final Column<PlayerLog> logInTime = grid.addColumn(new LocalDateTimeRenderer<>(
				PlayerLog::getLogInTime,
				"dd.MM.yyyy HH:mm:ss"))
				.setAutoWidth(true)
				.setHeader("Log In Time")
				.setSortProperty("logInTime");
		final Column<PlayerLog> logOutTime = grid.addColumn(new LocalDateTimeRenderer<>(
				PlayerLog::getLogOutTime,
				"dd.MM.yyyy HH:mm:ss"))
				.setAutoWidth(true)
				.setHeader("Log Out Time")
				.setSortProperty("logOutTime");
		grid.addColumn(new NumberRenderer<>(PlayerLog::getDuration, "%s min"))
				.setAutoWidth(true)
				.setHeader("Duration");

		grid.sort(List.of(new GridSortOrder<>(logInTime, SortDirection.DESCENDING)));
		grid.setMultiSort(true, true);

		grid.setItems(query -> playerLogService.list(
				PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
				filters).stream());
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

		return grid;
	}

	private void refreshGrid() {
		grid.getDataProvider().refreshAll();
	}

}
