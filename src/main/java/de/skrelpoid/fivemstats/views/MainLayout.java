package de.skrelpoid.fivemstats.views;

import java.util.Optional;
import org.vaadin.lineawesome.LineAwesomeIcon;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.skrelpoid.fivemstats.components.appnav.AppNav;
import de.skrelpoid.fivemstats.components.appnav.AppNavItem;
import de.skrelpoid.fivemstats.data.entity.User;
import de.skrelpoid.fivemstats.security.AuthenticatedUser;
import de.skrelpoid.fivemstats.views.dashboard.DashboardView;
import de.skrelpoid.fivemstats.views.groups.GroupsView;
import de.skrelpoid.fivemstats.views.history.HistoryView;
import de.skrelpoid.fivemstats.views.logs.LogsView;
import de.skrelpoid.fivemstats.views.players.PlayersView;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

	private static final long serialVersionUID = 1L;

	private H2 viewTitle;

	private final AuthenticatedUser authenticatedUser;
	private final AccessAnnotationChecker accessChecker;

	public MainLayout(final AuthenticatedUser authenticatedUser, final AccessAnnotationChecker accessChecker) {
		this.authenticatedUser = authenticatedUser;
		this.accessChecker = accessChecker;

		this.setId("appLayout");
		this.getElement().setAttribute("no-scroll", "");

		setPrimarySection(Section.DRAWER);
		addDrawerContent();
		addHeaderContent();
	}

	private void addHeaderContent() {
		final DrawerToggle toggle = new DrawerToggle();
		toggle.getElement().setAttribute("aria-label", "Menu toggle");

		viewTitle = new H2();
		viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

		addToNavbar(true, toggle, viewTitle);
	}

	private void addDrawerContent() {
		final H1 appName = new H1("FiveMStats");
		appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
		final Header header = new Header(appName);

		final Scroller scroller = new Scroller(createNavigation());
		scroller.setId("appLayoutScroller");

		addToDrawer(header, scroller, createFooter());
	}

	private AppNav createNavigation() {
		// AppNav is not an official component.
		// For documentation, visit https://github.com/vaadin/vcf-nav#readme
		// Starting with v24.1, AppNav will be replaced with the official
		// SideNav component.
		final AppNav nav = new AppNav();

		if (accessChecker.hasAccess(DashboardView.class)) {
			nav.addItem(new AppNavItem("Dashboard", DashboardView.class, LineAwesomeIcon.HOME_SOLID.create()));

		}
		if (accessChecker.hasAccess(HistoryView.class)) {
			nav.addItem(new AppNavItem("History", HistoryView.class, LineAwesomeIcon.HISTORY_SOLID.create()));

		}
		if (accessChecker.hasAccess(PlayersView.class)) {
			nav.addItem(new AppNavItem("Players", PlayersView.class, LineAwesomeIcon.USER_FRIENDS_SOLID.create()));

		}
		if (accessChecker.hasAccess(GroupsView.class)) {
			nav.addItem(new AppNavItem("Groups", GroupsView.class, LineAwesomeIcon.USERS_SOLID.create()));
		}
		if (accessChecker.hasAccess(LogsView.class)) {
			nav.addItem(new AppNavItem("Logs", LogsView.class, LineAwesomeIcon.FILE.create()));
		}

		return nav;
	}

	private Footer createFooter() {
		final Footer layout = new Footer();

		final Optional<User> maybeUser = authenticatedUser.get();
		if (maybeUser.isPresent()) {
			final User user = maybeUser.get();

			final Avatar avatar = new Avatar(user.getName());
			avatar.setThemeName("xsmall");
			avatar.getElement().setAttribute("tabindex", "-1");

			final MenuBar userMenu = new MenuBar();
			userMenu.setThemeName("tertiary-inline contrast");

			final MenuItem userName = userMenu.addItem("");
			final Div div = new Div();
			div.add(avatar);
			div.add(user.getName());
			div.add(new Icon("lumo", "dropdown"));
			div.getElement().getStyle().set("display", "flex");
			div.getElement().getStyle().set("align-items", "center");
			div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
			userName.add(div);
			userName.getSubMenu().addItem("Sign out", e -> {
				authenticatedUser.logout();
			});

			layout.add(userMenu);
		} else {
			final Anchor loginLink = new Anchor("login", "Sign in");
			layout.add(loginLink);
		}

		return layout;
	}

	@Override
	protected void afterNavigation() {
		super.afterNavigation();
		viewTitle.setText(getCurrentPageTitle());
	}

	private String getCurrentPageTitle() {
		final PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
		return title == null ? "" : title.value();
	}
}
