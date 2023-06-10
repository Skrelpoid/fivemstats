package de.skrelpoid.fivemstats.views.history;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;

import de.skrelpoid.fivemstats.data.entity.PlayerLog;
import de.skrelpoid.fivemstats.data.service.PlayerLogService;
import de.skrelpoid.fivemstats.data.service.PlayerService;
import de.skrelpoid.fivemstats.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@PageTitle("History")
@Route(value = "history", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class HistoryView extends Div {
	private static final long serialVersionUID = 1L;

	private Grid<PlayerLog> grid;

	private final Filters filters;
	private final PlayerLogService playerLogService;

	public HistoryView(final PlayerLogService playerLogService, final PlayerService playerService) {
		this.playerLogService = playerLogService;
		playerLogService.calculateAllLoggedInTime();
		setSizeFull();
		addClassNames("history-view");

		filters = new Filters(this::refreshGrid, playerService);
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

	public static class Filters extends Div implements Specification<PlayerLog> {

		private static final long serialVersionUID = 1L;
		private final TextField search = new TextField("Search");
		private final DatePicker startDate = new DatePicker("Period");
		private final DatePicker endDate = new DatePicker();
		private final MultiSelectComboBox<String> occupations = new MultiSelectComboBox<>("Occupation");
		private final CheckboxGroup<String> roles = new CheckboxGroup<>("Role");
		
		private final PlayerService playerService;

		public Filters(final Runnable onSearch, final PlayerService playerService) {
			this.playerService = playerService;
			setWidthFull();
			addClassName("filter-layout");
			addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
					LumoUtility.BoxSizing.BORDER);
			search.setPlaceholder("Name or Aliases or Identifiers");

			occupations.setItems("Insurance Clerk", "Mortarman", "Beer Coil Cleaner", "Scale Attendant");

			roles.setItems("Worker", "Supervisor", "Manager", "External");
			roles.addClassName("double-width");

			// Action buttons
			final Button resetBtn = new Button("Reset");
			resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
			resetBtn.addClickListener(e -> {
				search.clear();
				startDate.clear();
				endDate.clear();
				occupations.clear();
				roles.clear();
				onSearch.run();
			});
			final Button searchBtn = new Button("Search");
			searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			searchBtn.addClickListener(e -> onSearch.run());

			final Div actions = new Div(resetBtn, searchBtn);
			actions.addClassName(LumoUtility.Gap.SMALL);
			actions.addClassName("actions");

			add(search, createDateRangeFilter(), occupations, roles, actions);
		}

		private Component createDateRangeFilter() {
			startDate.setPlaceholder("From");
			final ValueChangeListener<ComponentValueChangeEvent<DatePicker, LocalDate>> listener = event -> {
				if (event.isFromClient()) {
					final LocalDate start = startDate.getValue();
					final LocalDate end = endDate.getValue();
					if (start != null && end != null && start.isAfter(end)) {
						startDate.setValue(end);
						endDate.setValue(start);
					}
				}
			};
			startDate.addValueChangeListener(listener);

			endDate.setPlaceholder("To");
			endDate.addValueChangeListener(listener);

			// For screen readers
			setAriaLabel(startDate, "From date");
			setAriaLabel(endDate, "To date");

			final FlexLayout dateRangeComponent = new FlexLayout(startDate, new Text(" – "), endDate);
			dateRangeComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
			dateRangeComponent.addClassName(LumoUtility.Gap.XSMALL);

			return dateRangeComponent;
		}

		private void setAriaLabel(final DatePicker datePicker, final String label) {
			datePicker.getElement().executeJs("const input = this.inputElement;" //
					+ "input.setAttribute('aria-label', $0);" //
					+ "input.removeAttribute('aria-labelledby');", label);
		}

		@Override
		public Predicate toPredicate(final Root<PlayerLog> root, final CriteriaQuery<?> query,
				final CriteriaBuilder criteriaBuilder) {
			final List<Predicate> predicates = new ArrayList<>();

			if (!search.isEmpty()) {
				final Predicate playerSearch = playerService.buildSearchPredicate(search.getValue().toLowerCase(), root, criteriaBuilder, "player");
				predicates.add(playerSearch);
			}
			if (startDate.getValue() != null && endDate.getValue() != null) {
				final LocalDateTime start = startDate.getValue().atStartOfDay();
				final LocalDateTime end = endDate.getValue().atTime(LocalTime.MAX);
				buildTimePeriodPredicates(root, criteriaBuilder, predicates, start, end);
			} else if (startDate.getValue() != null || endDate.getValue() != null) {
				final LocalDate value = startDate.getValue() != null ? startDate.getValue() : endDate.getValue();
				final LocalDateTime start = value.atStartOfDay();
				final LocalDateTime end = value.atTime(LocalTime.MAX);
				buildTimePeriodPredicates(root, criteriaBuilder, predicates, start, end);
			}
			if (!occupations.isEmpty()) {
				final String databaseColumn = "occupation";
				final List<Predicate> occupationPredicates = new ArrayList<>();
				for (final String occupation : occupations.getValue()) {
					occupationPredicates
							.add(criteriaBuilder.equal(criteriaBuilder.literal(occupation), root.get(databaseColumn)));
				}
				predicates.add(criteriaBuilder.or(occupationPredicates.toArray(Predicate[]::new)));
			}
			if (!roles.isEmpty()) {
				final String databaseColumn = "role";
				final List<Predicate> rolePredicates = new ArrayList<>();
				for (final String role : roles.getValue()) {
					rolePredicates.add(criteriaBuilder.equal(criteriaBuilder.literal(role), root.get(databaseColumn)));
				}
				predicates.add(criteriaBuilder.or(rolePredicates.toArray(Predicate[]::new)));
			}
			return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
		}

		protected void buildTimePeriodPredicates(final Root<PlayerLog> root, final CriteriaBuilder criteriaBuilder,
				final List<Predicate> predicates, final LocalDateTime start, final LocalDateTime end) {
			final LocalDateTime endToday = LocalDate.now().atTime(LocalTime.MAX);
			predicates.add(criteriaBuilder.lessThan(root.get("logInTime"), criteriaBuilder.literal(end)));
			final Predicate notNull = criteriaBuilder.lessThan(criteriaBuilder.literal(start), root.get("logOutTime"));
			final Predicate ifNull = criteriaBuilder.lessThan(criteriaBuilder.literal(start),
					criteriaBuilder.literal(endToday));
			final Predicate isNull = criteriaBuilder.isNull(root.get("logOutTime"));
			predicates.add(criteriaBuilder.or(notNull, criteriaBuilder.and(isNull, ifNull)));
		}

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
