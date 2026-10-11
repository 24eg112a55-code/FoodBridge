window.DonorApp = {
  async init(page) {
    try {
      if (page === "dashboard") await this.dashboard();
      else if (page === "donate-food") this.donationForm();
      else if (page === "my-donations" || page === "donation-history") await this.list(page === "donation-history");
      else if (page === "profile") this.profile();
    } catch (error) { setContent(errorBlock(error)); }
  },
  async dashboard() {
    const stats = await Api.get("/donor/dashboard");
    setContent(`<section class="welcome-row"><div><p class="eyebrow dark-eyebrow">YOUR CONTRIBUTION</p><h2>Good to see you, ${escapeHtml((Auth.getUser()?.name || "neighbor").split(" ")[0])}.</h2><p>Every listing is a chance to turn extra food into a shared meal.</p></div><a class="btn btn-dark-green" href="donate-food.html"><i class="bi bi-plus-lg"></i> Share food</a></section><section class="stats-grid">${this.stat("Total donations", stats.totalDonations, "bi-basket2", "green")}${this.stat("Available", stats.availableDonations, "bi-broadcast", "lime")}${this.stat("Accepted", stats.acceptedDonations, "bi-hand-thumbs-up", "orange")}${this.stat("Completed", stats.completedDonations, "bi-check2-circle", "blue")}</section><section class="content-panel dashboard-callout"><div class="callout-icon"><i class="bi bi-lightbulb"></i></div><div><h3>Have good food to share?</h3><p>Add pickup details and an expiry time. Nearby volunteers can take it from there.</p></div><a class="text-link" href="donate-food.html">Create a donation <i class="bi bi-arrow-right"></i></a></section>`);
  },
  stat(label, value, icon, color) { return `<article class="stat-card stat-${color}"><span class="stat-icon"><i class="bi ${icon}"></i></span><div><span>${label}</span><strong>${Number(value || 0).toLocaleString()}</strong></div></article>`; },
  donationForm() {
    setContent(`<section class="page-intro"><p class="eyebrow dark-eyebrow">DONOR / NEW LISTING</p><h2>Share food with your community.</h2><p>Let volunteers know what is ready, where to collect it and when it needs to be shared.</p></section><form id="donation-form" class="content-panel form-panel"><div id="form-message"></div><div class="form-grid"><label>Food name<input name="foodName" maxlength="120" required placeholder="e.g. Fresh vegetable meals"></label><label>Food type<select name="foodType" required><option value="VEGETARIAN">Vegetarian</option><option value="NON_VEGETARIAN">Non-vegetarian</option><option value="VEGAN">Vegan</option><option value="OTHER">Other</option></select></label><label>Description<textarea name="description" maxlength="1200" rows="3" required placeholder="Describe the food and how it has been stored"></textarea></label><label>Quantity<div class="input-combo"><input name="quantity" type="number" min="0.01" step="0.01" required placeholder="0"><select name="quantityUnit"><option>servings</option><option>meals</option><option>kg</option><option>boxes</option><option>packages</option></select></div></label><label>Expiry date and time<input name="expiryTime" type="datetime-local" required></label><label>Pickup address<input name="pickupAddress" maxlength="300" required placeholder="Street address"></label><label>City<input name="city" maxlength="100" required placeholder="City"></label><label>Pickup date<input name="pickupDate" type="date" required></label><label>Pickup time<input name="pickupTime" type="time" required></label><label class="form-span">Additional instructions<textarea name="additionalInstructions" maxlength="1200" rows="2" placeholder="Entry details, storage notes, contact instructions"></textarea></label></div><div class="form-actions"><a href="dashboard.html" class="btn btn-outline-secondary">Cancel</a><button class="btn btn-dark-green" type="submit"><i class="bi bi-send"></i> Post donation</button></div></form>`);
    const form = document.querySelector("#donation-form");
    form.pickupDate.min = new Date().toISOString().slice(0, 10);
    form.addEventListener("submit", async event => {
      event.preventDefault(); const button = form.querySelector("button[type=submit]"); button.disabled = true;
      const values = Object.fromEntries(new FormData(form)); values.quantity = Number(values.quantity);
      try { await Api.post("/donor/donations", values); toast("Donation posted and available to volunteers."); location.assign("my-donations.html"); }
      catch (error) { document.querySelector("#form-message").innerHTML = alertHtml(error.message); button.disabled = false; }
    });
  },
  async list(history) {
    const donations = await Api.get("/donor/donations");
    const items = history ? donations.filter(d => ["COMPLETED", "CANCELLED"].includes(d.status)) : donations.filter(d => !["COMPLETED", "CANCELLED"].includes(d.status));
    setContent(`<section class="page-intro intro-inline"><div><p class="eyebrow dark-eyebrow">${history ? "PAST LISTINGS" : "YOUR ACTIVE LISTINGS"}</p><h2>${history ? "Donation history" : "My donations"}</h2><p>${items.length} ${items.length === 1 ? "listing" : "listings"} ${history ? "in your history" : "in progress"}.</p></div>${!history ? `<a class="btn btn-dark-green" href="donate-food.html"><i class="bi bi-plus-lg"></i> New donation</a>` : ""}</section>${items.length ? `<div class="content-panel table-panel"><div class="table-responsive"><table class="table app-table"><thead><tr><th>Donation</th><th>Quantity</th><th>Pickup</th><th>Expiry</th><th>Volunteer</th><th>Status</th><th></th></tr></thead><tbody>${items.map(d => `<tr><td><strong>#${d.id} · ${escapeHtml(d.foodName)}</strong><small>${escapeHtml(d.foodType.replaceAll("_", " "))} · ${formatDate(d.createdAt)}</small></td><td>${escapeHtml(d.quantity)} ${escapeHtml(d.quantityUnit)}</td><td>${escapeHtml(d.city)}<small>${formatDate(`${d.pickupDate}T${d.pickupTime}`, true)}</small></td><td>${formatDate(d.expiryTime, true)}</td><td>${escapeHtml(d.volunteerName || "Not assigned")}</td><td>${statusBadge(d.status)}</td><td>${d.status === "AVAILABLE" ? `<button class="btn btn-sm btn-outline-danger" data-cancel="${d.id}">Cancel</button>` : ""}</td></tr>`).join("")}</tbody></table></div></div>` : `<div class="empty-state"><span><i class="bi bi-basket"></i></span><h3>${history ? "Your history is ready to grow" : "No donations yet"}</h3><p>${history ? "Completed and cancelled listings will appear here." : "Post a donation and a volunteer can arrange a pickup."}</p>${!history ? `<a class="btn btn-dark-green" href="donate-food.html">Share food</a>` : ""}</div>`}`);
    document.querySelectorAll("[data-cancel]").forEach(button => button.addEventListener("click", async () => {
      if (!confirm("Cancel this available donation?")) return;
      button.disabled = true;
      try { await Api.put(`/donor/donations/${button.dataset.cancel}/cancel`); toast("Donation cancelled."); await this.list(history); }
      catch (error) { toast(error.message, "danger"); button.disabled = false; }
    }));
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