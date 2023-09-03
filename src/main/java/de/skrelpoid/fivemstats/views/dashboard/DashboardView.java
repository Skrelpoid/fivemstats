package de.skrelpoid.fivemstats.views.dashboard;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.Scroller.ScrollDirection;
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
	private ApexCharts charts;

	private Scroller scroller;
	private final Div progress;

	private final PlayerLogService playerLogService;
	private final PlayerService playerService;

	public DashboardView(final PlayerLogService playerLogService, final PlayerService playerService) {
		this.playerLogService = playerLogService;
		this.playerService = playerService;
		setMargin(true);
		setId("dashboardLayout");

		setHeightFull();
		progress = buildProgress();

		add(progress);
	}

	private Div buildProgress() {
		final Div container = new Div();
		container.addClassName("progressContainer");
		container.setSizeFull();

		final Label label = new Label("Loading Data, please stand by...");
		container.add(label);

		final Div spinner = new Div();
		spinner.addClassName("spinner");

		final Div cube1 = new Div();
		cube1.addClassName("cube1");

		final Div cube2 = new Div();
		cube2.addClassName("cube2");

		spinner.add(cube1, cube2);
		container.add(label, spinner);
		return container;
	}

	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		final Executor exec = Executors.newSingleThreadExecutor();

		exec.execute(() -> {

			final Map<Long, Player> players = playerService.findAllAndGroupByID();
			// TODO limit
			final List<PlayerLogSeconds> data =
					playerLogService.calculateAllLoggedInTime()
							.stream()
							.limit(250)
							.toList();

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
					data.stream()
							.map(PlayerLogSeconds::getPlayerId)
							.map(id -> players.get(id).getName())
							.toList());

			builder.withChart(chart);
			builder.withSeries(new Series<>("Minutes Logged", data.stream().map(PlayerLogSeconds::getCumulatedSeconds)
					.map(Long::doubleValue).map(d -> Math.max(Math.round(d / 60), 1D)).toArray(Double[]::new)));
			builder.withXaxis(xaxis);
			builder.withDataLabels(DataLabelsBuilder.get().withEnabled(true).build());
			builder.withPlotOptions(
					PlotOptionsBuilder.get().withBar(BarBuilder.get().withHorizontal(true).build()).build());

			scroller = new Scroller();
			scroller.setId("chartScroller");

			attachEvent.getUI().access(() -> {
				charts = builder.build();
				charts.setWidthFull();
				scroller.setContent(charts);
				scroller.setScrollDirection(ScrollDirection.VERTICAL);
				scroller.setWidthFull();
				scroller.setHeight("90%");
				remove(progress);
				add(scroller);
				final Element element = scroller.getElement();
				element.executeJs("setTimeout(function() {$1.scrollLeft += 1000;}, 1000)", element);
				attachEvent.getUI().push();
			});
		});
	}

}
