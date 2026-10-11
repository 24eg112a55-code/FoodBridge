window.VolunteerApp = {
  async init(page) {
    try {
      if (page === "dashboard") await this.dashboard();
      else if (page === "available-donations") await this.available();
      else if (page === "my-pickups") await this.pickups();
      else if (page === "completed-deliveries") await this.completed();
      else if (page === "profile") this.profile();
    } catch (error) { setContent(errorBlock(error)); }
  },
  async dashboard() {
    const stats = await Api.get("/volunteer/dashboard");
    setContent(`<section class="welcome-row"><div><p class="eyebrow dark-eyebrow">YOUR COMMUNITY ROUTE</p><h2>Good to see you, ${escapeHtml((Auth.getUser()?.name || "neighbor").split(" ")[0])}.</h2><p>Every pickup makes good food go further.</p></div><a class="btn btn-dark-green" href="available-donations.html"><i class="bi bi-search"></i> Find a pickup</a></section><section class="stats-grid">${this.stat("Available food", stats.availableDonations, "bi-basket2", "green")}${this.stat("My accepted", stats.myAcceptedDonations, "bi-box-seam", "lime")}${this.stat("Completed", stats.completedDeliveries, "bi-check2-circle", "blue")}${this.stat("People served", stats.peopleServed, "bi-people", "orange")}</section><section class="content-panel dashboard-callout"><div class="callout-icon"><i class="bi bi-signpost-split"></i></div><div><h3>Ready for your next route?</h3><p>Browse open donations and take a pickup that fits your schedule.</p></div><a class="text-link" href="available-donations.html">Browse available food <i class="bi bi-arrow-right"></i></a></section>`);
  },
  stat(label, value, icon, color) { return `<article class="stat-card stat-${color}"><span class="stat-icon"><i class="bi ${icon}"></i></span><div><span>${label}</span><strong>${Number(value || 0).toLocaleString()}</strong></div></article>`; },
  async available() {
    const items = await Api.get("/volunteer/donations/available");
    setContent(`<section class="page-intro"><p class="eyebrow dark-eyebrow">NEARBY OPPORTUNITIES</p><h2>Available food</h2><p>${items.length} donations are waiting for a volunteer pickup.</p></section>${items.length ? `<div class="donation-card-grid">${items.map(d => `<article class="food-card"><div class="food-card-top"><span class="food-type"><i class="bi bi-flower1"></i> ${escapeHtml(d.foodType.replaceAll("_", " "))}</span>${statusBadge(d.status)}</div><h3>${escapeHtml(d.foodName)}</h3><p class="food-description">${escapeHtml(d.description)}</p><div class="food-meta"><span><i class="bi bi-box2"></i>${escapeHtml(d.quantity)} ${escapeHtml(d.quantityUnit)}</span><span><i class="bi bi-geo-alt"></i>${escapeHtml(d.city)}</span><span><i class="bi bi-calendar3"></i>${formatDate(`${d.pickupDate}T${d.pickupTime}`, true)}</span><span><i class="bi bi-hourglass-split"></i>Expires ${formatDate(d.expiryTime, true)}</span><span><i class="bi bi-person"></i>${escapeHtml(d.donorName)}</span></div><div class="food-address"><i class="bi bi-pin-map"></i>${escapeHtml(d.pickupAddress)}</div><button class="btn btn-dark-green w-100" data-accept="${d.id}"><i class="bi bi-hand-thumbs-up"></i> Accept donation</button></article>`).join("")}</div>` : `<div class="empty-state"><span><i class="bi bi-basket"></i></span><h3>Nothing waiting right now</h3><p>New donations from your community will show up here.</p></div>`}`);
    document.querySelectorAll("[data-accept]").forEach(button => button.addEventListener("click", async () => {
      button.disabled = true; button.innerHTML = `<span class="spinner-border spinner-border-sm"></span> Accepting`;
      try { await Api.put(`/volunteer/donations/${button.dataset.accept}/accept`); toast("Pickup added to your route."); await this.available(); }
      catch (error) { toast(error.message, "danger"); button.disabled = false; button.innerHTML = `<i class="bi bi-hand-thumbs-up"></i> Accept donation`; }
    }));
  },
  async pickups() {
    const items = await Api.get("/volunteer/donations/my-pickups");
    setContent(`<section class="page-intro"><p class="eyebrow dark-eyebrow">YOUR ACTIVE ROUTE</p><h2>My pickups</h2><p>Pickup, distribution and completion steps for your accepted donations.</p></section>${items.length ? `<div class="pickup-list">${items.map(d => this.pickupCard(d)).join("")}</div>` : `<div class="empty-state"><span><i class="bi bi-box-seam"></i></span><h3>Your route is clear</h3><p>Accept an available donation to see the pickup details here.</p><a class="btn btn-dark-green" href="available-donations.html">Browse food</a></div>`}`);
    this.bindWorkflow();
  },
  pickupCard(d) {
    let action = "";
    if (d.status === "ACCEPTED") action = `<button class="btn btn-dark-green" data-pickup="${d.id}"><i class="bi bi-box-arrow-in-down"></i> Mark as picked up</button>`;
    if (d.status === "PICKED_UP") action = `<form class="distribution-form" data-distribute="${d.id}"><h4>Record the distribution</h4><div class="form-grid"><label>Distribution location<input name="distributionLocation" required maxlength="300"></label><label>NGO, shelter or community name<input name="organizationName" required maxlength="160"></label><label>People served<input name="peopleServed" type="number" min="1" max="100000" required></label><label>Distribution notes<textarea name="distributionNotes" rows="2" maxlength="1200"></textarea></label></div><button class="btn btn-dark-green" type="submit"><i class="bi bi-send-check"></i> Mark as distributed</button></form>`;
    if (d.status === "DISTRIBUTED") action = `<div class="distribution-summary"><span><i class="bi bi-people"></i> ${escapeHtml(d.peopleServed)} people served at ${escapeHtml(d.organizationName)}</span><button class="btn btn-dark-green" data-complete="${d.id}"><i class="bi bi-check2"></i> Complete donation</button></div>`;
    return `<article class="content-panel pickup-card"><div class="pickup-head"><div><span class="eyebrow dark-eyebrow">DONATION #${d.id}</span><h3>${escapeHtml(d.foodName)}</h3></div>${statusBadge(d.status)}</div><div class="pickup-details"><div><span>DONOR</span><strong>${escapeHtml(d.donorName)}</strong><small>${escapeHtml(d.donorPhone)}</small></div><div><span>QUANTITY</span><strong>${escapeHtml(d.quantity)} ${escapeHtml(d.quantityUnit)}</strong><small>${escapeHtml(d.foodType.replaceAll("_", " "))}</small></div><div><span>PICKUP</span><strong>${formatDate(`${d.pickupDate}T${d.pickupTime}`, true)}</strong><small>${escapeHtml(d.pickupAddress)}, ${escapeHtml(d.city)}</small></div><div><span>EXPIRY</span><strong>${formatDate(d.expiryTime, true)}</strong><small>${escapeHtml(d.additionalInstructions || "No additional instructions")}</small></div></div>${action ? `<div class="pickup-action">${action}</div>` : ""}</article>`;
  },
  bindWorkflow() {
    document.querySelectorAll("[data-pickup]").forEach(button => button.addEventListener("click", async () => this.runAction(button, `/volunteer/donations/${button.dataset.pickup}/pickup`, "Pickup marked. Next, record where the food was distributed.")));
    document.querySelectorAll("[data-complete]").forEach(button => button.addEventListener("click", async () => this.runAction(button, `/volunteer/donations/${button.dataset.complete}/complete`, "Donation completed. Thank you for delivering.")));
    document.querySelectorAll("[data-distribute]").forEach(form => form.addEventListener("submit", async event => {
      event.preventDefault(); const button = form.querySelector("button[type=submit]"); button.disabled = true;
      const body = Object.fromEntries(new FormData(form)); body.peopleServed = Number(body.peopleServed);
      try { await Api.put(`/volunteer/donations/${form.dataset.distribute}/distribute`, body); toast("Distribution recorded."); await this.pickups(); }
      catch (error) { toast(error.message, "danger"); button.disabled = false; }
    }));
  },
  async runAction(button, path, message) { button.disabled = true; try { await Api.put(path); toast(message); await this.pickups(); } catch (error) { toast(error.message, "danger"); button.disabled = false; } },
  async completed() {
    const items = await Api.get("/volunteer/donations/completed");
    setContent(`<section class="page-intro"><p class="eyebrow dark-eyebrow">DELIVERED WITH CARE</p><h2>Completed deliveries</h2><p>${items.length} completed ${items.length === 1 ? "delivery" : "deliveries"}.</p></section>${items.length ? `<div class="content-panel table-panel"><div class="table-responsive"><table class="table app-table"><thead><tr><th>Food</th><th>Destination</th><th>People served</th><th>Completed</th></tr></thead><tbody>${items.map(d => `<tr><td><strong>${escapeHtml(d.foodName)}</strong><small>${escapeHtml(d.quantity)} ${escapeHtml(d.quantityUnit)}</small></td><td>${escapeHtml(d.organizationName || "—")}<small>${escapeHtml(d.distributionLocation || "")}</small></td><td>${Number(d.peopleServed || 0).toLocaleString()}</td><td>${statusBadge(d.status)}<small>${formatDate(d.completedAt, true)}</small></td></tr>`).join("")}</tbody></table></div></div>` : `<div class="empty-state"><span><i class="bi bi-check2-circle"></i></span><h3>No completed deliveries yet</h3><p>Finished distributions will be recorded here.</p></div>`}`);
  },
  async profile() {
    const user = await Api.get("/profile");
    setContent(`<section class="page-intro"><p class="eyebrow dark-eyebrow">YOUR ACCOUNT</p><h2>Profile</h2><p>Keep your contact details current for smooth food pickups.</p></section><form id="profile-form" class="content-panel profile-form"><span class="profile-avatar">${escapeHtml(user.fullName.slice(0, 1).toUpperCase())}</span><label>Full name<input name="fullName" value="${escapeHtml(user.fullName)}" required maxlength="120"></label><label>Email address<input value="${escapeHtml(user.email)}" disabled></label><label>Phone number<input name="phone" value="${escapeHtml(user.phone)}" required minlength="7" maxlength="32"></label><div><span class="profile-label">ACCOUNT TYPE</span>${statusBadge(user.role)}</div><button class="btn btn-dark-green" type="submit"><i class="bi bi-check2"></i> Save profile</button><div id="profile-message"></div></form>`);
    document.querySelector("#profile-form").addEventListener("submit", async event => {
      event.preventDefault(); const form = event.currentTarget; const button = form.querySelector("button"); button.disabled = true;
      try { const saved = await Api.put("/profile", Object.fromEntries(new FormData(form))); const info = Auth.getUser() || {}; localStorage.setItem("cfs_user", JSON.stringify({ ...info, name: saved.fullName })); toast("Profile updated."); await this.profile(); }
      catch (error) { document.querySelector("#profile-message").innerHTML = alertHtml(error.message); button.disabled = false; }
    });
  }
};