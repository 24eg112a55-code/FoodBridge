const Auth = {
  getToken() { return localStorage.getItem("cfs_token"); },
  getRole() { return localStorage.getItem("cfs_role"); },
  getUser() { try { return JSON.parse(localStorage.getItem("cfs_user") || "null"); } catch { return null; } },
  isAuthenticated() { return Boolean(this.getToken()); },
  async login(email, password) {
    const data = await Api.post("/auth/login", { email, password });
    localStorage.setItem("cfs_token", data.token);
    localStorage.setItem("cfs_role", data.role);
    localStorage.setItem("cfs_user", JSON.stringify({ name: data.name, email: data.email }));
    const route = { DONOR: "donor/dashboard.html", VOLUNTEER: "volunteer/dashboard.html", ADMIN: "admin/dashboard.html" };
    window.location.assign(route[data.role] || "login.html");
  },
  async register(payload) { return Api.post("/auth/register", payload); },
  logout(expired = false) {
    localStorage.removeItem("cfs_token"); localStorage.removeItem("cfs_role"); localStorage.removeItem("cfs_user");
    if (!location.pathname.endsWith("login.html")) location.assign(`${location.pathname.includes("/donor/") || location.pathname.includes("/volunteer/") || location.pathname.includes("/admin/") ? "../" : ""}login.html${expired ? "?expired=1" : ""}`);
  },
  requireAuth() { if (!this.isAuthenticated()) { location.replace(location.pathname.includes("/donor/") || location.pathname.includes("/volunteer/") || location.pathname.includes("/admin/") ? "../login.html" : "login.html"); return false; } return true; },
  requireRole(role) {
    if (!this.requireAuth()) return false;
    if (this.getRole() !== role) { const route = { DONOR: "donor/dashboard.html", VOLUNTEER: "volunteer/dashboard.html", ADMIN: "admin/dashboard.html" }; location.replace(`../${route[this.getRole()] || "login.html"}`); return false; }
    return true;
  },
  bindLogin() {
    loadTypeLayout();
    if (this.isAuthenticated()) { const route = { DONOR: "donor/dashboard.html", VOLUNTEER: "volunteer/dashboard.html", ADMIN: "admin/dashboard.html" }; location.replace(route[this.getRole()] || "index.html"); return; }
    if (new URLSearchParams(location.search).has("registered")) showAuthAlert("Your account is ready. Sign in to continue.", "success");
    const form = document.querySelector("#login-form");
    form.addEventListener("submit", async event => {
      event.preventDefault(); const button = form.querySelector("button[type=submit]"); button.disabled = true;
      try { await this.login(form.email.value.trim(), form.password.value); }
      catch (error) { showAuthAlert(error.message, "danger"); button.disabled = false; }
    });
    if (new URLSearchParams(location.search).has("expired")) showAuthAlert("Your session ended. Please sign in again.", "warning");
  },
  bindRegister() {
    loadTypeLayout();
    const role = new URLSearchParams(location.search).get("role"); if (["DONOR", "VOLUNTEER"].includes(role)) document.querySelector("#role").value = role;
    const form = document.querySelector("#register-form");
    form.addEventListener("submit", async event => {
      event.preventDefault();
      if (form.password.value !== form.confirmPassword.value) { showAuthAlert("Passwords do not match.", "danger"); return; }
      const button = form.querySelector("button[type=submit]"); button.disabled = true;
      try { await this.register({ fullName: form.fullName.value.trim(), email: form.email.value.trim(), phone: form.phone.value.trim(), password: form.password.value, role: form.role.value }); location.assign("login.html?registered=1"); }
      catch (error) { showAuthAlert(error.message, "danger"); button.disabled = false; }
    });
  }
};
function loadTypeLayout() {
  const link = document.createElement("link"); link.rel = "stylesheet"; link.href = "css/type-layout.css"; document.head.append(link);
}
function showAuthAlert(message, type) { const alert = document.querySelector("#auth-alert"); alert.className = `alert alert-${type}`; alert.textContent = message; }