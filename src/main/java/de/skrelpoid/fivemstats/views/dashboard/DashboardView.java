package de.skrelpoid.fivemstats.views.dashboard;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.Chart;
import com.github.appreciated.apexcharts.config.XAxis;
import com.github.appreciated.apexcharts.config.builder.DataLabelsBuilder;
import com.github.appreciated.apexcharts.config.builder.PlotOptionsBuilder;
import com.github.appreciated.apexcharts.config.builder.ThemeBuilder;
import com.github.appreciated.apexcharts.config.builder.TooltipBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.config.theme.Mode;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.Scroller.ScrollDirection;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import de.skrelpoid.fivemstats.data.PlayerLogSeconds;
import de.skrelpoid.fivemstats.data.entity.Player;
import de.skrelpoid.fivemstats.data.service.PlayerLogService;
import de.skrelpoid.fivemstats.data.service.PlayerService;
import de.skrelpoid.fivemstats.views.MainLayout;
import jakarta.annotation.security.PermitAll;

@PageTitle("Dashboard")
@Route(value = "dashboard", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
public class DashboardView extends HorizontalLayout {

	private static final Logger logger = LoggerFactory.getLogger(DashboardView.class);

	private static final long serialVersionUID = 1L;
	private final TextField name;
	private final Button sayHello;
	private final ApexCharts charts;

	private final Scroller scroller;

	public DashboardView(final PlayerLogService playerLogService, final PlayerService playerService) {
		name = new TextField("Your name");
		sayHello = new Button("Say hello");
		sayHello.addClickListener(e -> Notification.show("Hello " + name.getValue()));
		sayHello.addClickShortcut(Key.ENTER);

		setMargin(true);
		setVerticalComponentAlignment(Alignment.END, name, sayHello);
		final Map<Long, Player> players = playerService.findAllAndGroupByID();
		// TODO limit
		final List<PlayerLogSeconds> data = playerLogService.calculateAllLoggedInTime().subList(0, 50);

		// TODO show players missing with 0

		final ApexChartsBuilder builder = new ApexChartsBuilder();
		builder.withTooltip(TooltipBuilder.get().withTheme("dark").build());
		builder.withTheme(ThemeBuilder.get().withMode(Mode.DARK).withPalette("palette1").build());

		final Chart chart = new Chart();
		chart.setType(Type.BAR);
		chart.setHeight(String.valueOf(data.size() * 16));
		chart.setBackground("transparent");

		final XAxis xaxis = new XAxis();
		xaxis.setCategories(
				data.stream().map(PlayerLogSeconds::getPlayerId).map(id -> players.get(id).getName()).toList());

		builder.withChart(chart);
		builder.withSeries(new Series<>("Minutes Logged", data.stream().map(PlayerLogSeconds::getCumulatedSeconds)
				.map(Long::doubleValue).map(d -> Math.max(Math.round(d / 60), 1D)).toArray(Double[]::new)));
		builder.withXaxis(xaxis);
		builder.withDataLabels(DataLabelsBuilder.get().withEnabled(true).build());
		builder.withPlotOptions(
				PlotOptionsBuilder.get().withBar(BarBuilder.get().withHorizontal(true).build()).build());

		scroller = new Scroller();
		scroller.setId("chartScroller");
		charts = builder.build();
		charts.setWidthFull();
		scroller.setContent(charts);
		scroller.setScrollDirection(ScrollDirection.VERTICAL);
		scroller.setWidthFull();
		scroller.setHeight("50%");
		setHeightFull();
		add(name, sayHello, scroller);
	}

	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		final Element element = scroller.getElement();
		element.executeJs("setTimeout(function() {$1.scrollLeft += 1000;}, 1000)", element);
	}

}
