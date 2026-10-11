const pageTitles = {
  dashboard: "Dashboard", "donate-food": "Share food", "my-donations": "My donations", "donation-history": "Donation history", profile: "My profile",
  "available-donations": "Available food", "my-pickups": "My pickups", "completed-deliveries": "Completed deliveries",
  users: "People", volunteers: "Volunteers", donations: "Donations", pickups: "Pickups", "completed-distributions": "Completed distributions", reports: "Reports"
};
const menuItems = {
  DONOR: [["dashboard", "Dashboard", "bi-grid-1x2"], ["donate-food", "Donate food", "bi-plus-circle"], ["my-donations", "My donations", "bi-basket"], ["donation-history", "Donation history", "bi-clock-history"], ["profile", "Profile", "bi-person"]],
  VOLUNTEER: [["dashboard", "Dashboard", "bi-grid-1x2"], ["available-donations", "Available food", "bi-basket"], ["my-pickups", "My pickups", "bi-box-seam"], ["completed-deliveries", "Completed deliveries", "bi-check2-circle"], ["profile", "Profile", "bi-person"]],
  ADMIN: [["dashboard", "Dashboard", "bi-grid-1x2"], ["users", "Users", "bi-people"], ["volunteers", "Volunteers", "bi-bicycle"], ["donations", "Donations", "bi-basket"], ["pickups", "Pickups", "bi-box-seam"], ["completed-distributions", "Completed distributions", "bi-check2-circle"], ["reports", "Reports", "bi-bar-chart"]]
};
function escapeHtml(value) { return String(value ?? "").replace(/[&<>"']/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[char]); }
function formatDate(value, withTime = false) { if (!value) return "—"; const date = new Date(value); return Number.isNaN(date.valueOf()) ? escapeHtml(value) : new Intl.DateTimeFormat(undefined, withTime ? { dateStyle: "medium", timeStyle: "short" } : { dateStyle: "medium" }).format(date); }
function statusBadge(status) { return `<span class="status-badge status-${String(status).toLowerCase()}">${escapeHtml(String(status).replaceAll("_", " "))}</span>`; }
function alertHtml(message, kind = "danger") { return `<div class="alert alert-${kind}" role="alert">${escapeHtml(message)}</div>`; }
function toast(message, kind = "success") {
  let region = document.querySelector("#toast-region");
  if (!region) { region = document.createElement("div"); region.id = "toast-region"; region.className = "toast-region"; document.body.append(region); }
  const item = document.createElement("div"); item.className = `app-toast toast-${kind}`; item.textContent = message; region.append(item); setTimeout(() => item.remove(), 4200);
}
function setContent(html) { document.querySelector("#page-content").innerHTML = html; }
function loadingBlock() { return `<div class="loading-state"><span class="spinner-border spinner-border-sm" aria-hidden="true"></span> Loading your workspace…</div>`; }
function errorBlock(error) { return alertHtml(error.message || "Something went wrong. Please try again."); }
function renderFrame(role, page) {
  const user = Auth.getUser() || { name: "Community member", email: "" };
  const links = menuItems[role] || [];
  document.querySelector("#app-frame").innerHTML = `<aside class="sidebar" id="sidebar"><a class="brand side-brand" href="../index.html"><span class="brand-mark"><i class="bi bi-basket2-fill"></i></span> Food<span>Bridge</span></a><div class="side-caption">YOUR WORKSPACE</div><nav class="side-menu">${links.map(([key, label, icon]) => `<a href="${key === "dashboard" ? "dashboard.html" : `${key}.html`}" class="side-link ${key === page ? "active" : ""}" ${key === page ? 'aria-current="page"' : ""}><i class="bi ${icon}"></i><span>${label}</span></a>`).join("")}</nav><div class="sidebar-bottom"><div class="sidebar-note"><i class="bi bi-heart"></i><span>Small acts make<br>strong communities.</span></div><button class="side-logout" id="logout-button"><i class="bi bi-box-arrow-left"></i><span>Log out</span></button></div></aside><div class="workspace"><header class="workspace-topbar"><button class="icon-button mobile-menu" id="menu-toggle" aria-label="Open navigation"><i class="bi bi-list"></i></button><div><span class="topbar-label">${role.toLowerCase()} workspace</span><h1>${pageTitles[page] || "Workspace"}</h1></div><div class="topbar-user"><button class="icon-button notification-toggle" id="notifications-button" aria-label="Notifications" title="Notifications"><i class="bi bi-bell"></i><span id="notification-count" hidden></span></button><div class="notification-popover" id="notification-popover" hidden></div><span class="user-avatar">${escapeHtml((user.name || "M").slice(0, 1).toUpperCase())}</span><div><strong>${escapeHtml(user.name || "Member")}</strong><small>${escapeHtml(role.toLowerCase())}</small></div><button class="icon-button" id="logout-top" aria-label="Log out" title="Log out"><i class="bi bi-box-arrow-right"></i></button></div></header><main id="page-content" class="page-content">${loadingBlock()}</main></div>`;
  document.querySelectorAll("#logout-button, #logout-top").forEach(button => button.addEventListener("click", () => Auth.logout()));
  document.querySelector("#menu-toggle").addEventListener("click", () => document.querySelector("#sidebar").classList.toggle("sidebar-open"));
  document.querySelector("#notifications-button").addEventListener("click", async () => {
    const popover = document.querySelector("#notification-popover");
    popover.hidden = !popover.hidden;
    if (popover.hidden) return;
    popover.innerHTML = loadingBlock();
    try {
      const notifications = await Api.get("/notifications");
      const unread = notifications.filter(item => !item.read).length;
      const count = document.querySelector("#notification-count"); count.hidden = unread === 0; count.textContent = unread > 9 ? "9+" : String(unread);
      popover.innerHTML = `<div class="notification-heading"><strong>Notifications</strong><span>${unread} unread</span></div>${notifications.length ? notifications.map(item => `<button class="notification-item ${item.read ? "" : "unread"}" data-notification="${item.id}"><span>${escapeHtml(item.message)}</span><small>${formatDate(item.createdAt, true)}</small></button>`).join("") : `<p class="notification-empty">You’re all caught up.</p>`}`;
      popover.querySelectorAll("[data-notification]").forEach(item => item.addEventListener("click", async () => {
        try { await Api.put(`/notifications/${item.dataset.notification}/read`); item.classList.remove("unread"); }
        catch (error) { toast(error.message, "danger"); }
      }));
    } catch (error) { popover.innerHTML = alertHtml(error.message); }
  });
}
document.addEventListener("DOMContentLoaded", () => {
  const shell = document.querySelector("#app-frame");
  if (!shell) return;
  const interactionStyles = document.createElement("link");
  interactionStyles.rel = "stylesheet";
  interactionStyles.href = "../css/interactions.css";
  document.head.append(interactionStyles);
  const brandStyles = document.createElement("link");
  brandStyles.rel = "stylesheet";
  brandStyles.href = "../css/type-layout.css";
  document.head.append(brandStyles);
  const role = document.body.dataset.role; const page = document.body.dataset.page;
  if (!Auth.requireRole(role)) return;
  renderFrame(role, page);
  const modules = { DONOR: window.DonorApp, VOLUNTEER: window.VolunteerApp, ADMIN: window.AdminApp };
  modules[role]?.init(page);
});