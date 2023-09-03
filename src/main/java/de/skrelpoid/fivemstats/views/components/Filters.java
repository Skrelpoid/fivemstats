package de.skrelpoid.fivemstats.views.components;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.skrelpoid.fivemstats.data.entity.Group;
import de.skrelpoid.fivemstats.data.entity.Player;
import de.skrelpoid.fivemstats.data.entity.PlayerLog;
import de.skrelpoid.fivemstats.data.service.GroupService;
import de.skrelpoid.fivemstats.data.service.PlayerService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class Filters<T> extends Div implements Specification<T> {

	private static final long serialVersionUID = 1L;
	private final TextField search = new TextField("Search");
	private final DatePicker startDate = new DatePicker("Period");
	private final DatePicker endDate = new DatePicker();
	private final MultiSelectComboBox<Group> groups = new MultiSelectComboBox<>("Group");
	private final Button currentWeek = new Button("This Week");
	private final Button lastWeek = new Button("Last Week");
	private final Button currentMonth = new Button("This Month");
	private final Button lastMonth = new Button("Last Month");

	private final PlayerService playerService;
	private final GroupService groupService;
	private Class<T> clazz;

	public Filters(final Runnable onSearch, final PlayerService playerService, final GroupService groupService,
			final Class<T> clazz) {
		this.playerService = playerService;
		this.groupService = groupService;
		this.clazz = clazz;
		setWidthFull();
		addClassName("filter-layout");
		addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
				LumoUtility.BoxSizing.BORDER);
		search.setPlaceholder("Name or Aliases or Identifiers");

		groups.setItems(groupService.listAll());


		// Action buttons
		final Button resetBtn = new Button("Reset");
		resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		resetBtn.addClickListener(e -> {
			search.clear();
			startDate.clear();
			endDate.clear();
			groups.clear();
			onSearch.run();
		});
		final Button searchBtn = new Button("Search");
		searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		searchBtn.addClickListener(e -> onSearch.run());

		final Div actions = new Div(resetBtn, searchBtn);
		actions.addClassName(LumoUtility.Gap.SMALL);
		actions.addClassName("actions");

		currentWeek.addThemeVariants(ButtonVariant.LUMO_SMALL);
		lastWeek.addThemeVariants(ButtonVariant.LUMO_SMALL);
		currentMonth.addThemeVariants(ButtonVariant.LUMO_SMALL);
		lastMonth.addThemeVariants(ButtonVariant.LUMO_SMALL);

		final FlexLayout dateButtonFlex = new FlexLayout(new VerticalLayout(currentWeek, lastWeek),
				new VerticalLayout(currentMonth, lastMonth));
		dateButtonFlex.setAlignItems(Alignment.BASELINE);
		// dateButtonFlex.addClassName(LumoUtility.Gap.XSMALL);

		add(search, createDateRangeFilter(), dateButtonFlex, groups, actions);
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
		datePicker.getElement().executeJs("""
				const input = this.inputElement;\
				input.setAttribute('aria-label', $0);\
				input.removeAttribute('aria-labelledby');""", label);
	}

	@Override
	public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query,
			final CriteriaBuilder criteriaBuilder) {
		From<Player, Player> playerFrom;
		From<PlayerLog, PlayerLog> playerLogFrom;
		if (clazz == Player.class) {
			playerFrom = (Root<Player>) root;
			playerLogFrom = playerFrom.join("playerLogs");
		} else {
			playerLogFrom = (Root<PlayerLog>) root;
			playerFrom = playerLogFrom.join("player");
		}

		final List<Predicate> predicates = new ArrayList<>();

		if (!search.isEmpty()) {
			final Predicate playerSearch =
					playerService.buildSearchPredicate(search.getValue().toLowerCase(), playerFrom,
							criteriaBuilder);
			predicates.add(playerSearch);
		}
		if (startDate.getValue() != null && endDate.getValue() != null) {
			final LocalDateTime start = startDate.getValue().atStartOfDay();
			final LocalDateTime end = endDate.getValue().atTime(LocalTime.MAX);
			buildTimePeriodPredicates(playerLogFrom, criteriaBuilder, predicates, start, end);
		} else if (startDate.getValue() != null || endDate.getValue() != null) {
			final LocalDate value = startDate.getValue() != null ? startDate.getValue() : endDate.getValue();
			final LocalDateTime start = value.atStartOfDay();
			final LocalDateTime end = value.atTime(LocalTime.MAX);
			buildTimePeriodPredicates(playerLogFrom, criteriaBuilder, predicates, start, end);
		}
		if (!groups.isEmpty()) {
			Join<Object, Object> join = playerFrom.join("groups");
			final List<Predicate> groupPredicates = new ArrayList<>();
			for (final Group group : groups.getValue()) {
				groupPredicates
						.add(criteriaBuilder.equal(criteriaBuilder.literal(group.getId()), join.get("id")));
			}
			predicates.add(criteriaBuilder.or(groupPredicates.toArray(Predicate[]::new)));
		}
		return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
	}

	protected void buildTimePeriodPredicates(final From<PlayerLog, PlayerLog> root,
			final CriteriaBuilder criteriaBuilder,
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
