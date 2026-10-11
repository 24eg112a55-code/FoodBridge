const Api = {
  async request(path, options = {}) {
    const token = localStorage.getItem("cfs_token");
    const headers = new Headers(options.headers || {});
    if (options.body && !(options.body instanceof FormData)) headers.set("Content-Type", "application/json");
    if (token) headers.set("Authorization", `Bearer ${token}`);
    let response;
    try {
      response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers });
    } catch {
      throw new Error("Unable to reach the server. Check that the backend is running.");
    }
    const data = response.status === 204 ? null : await response.json().catch(() => null);
    if (!response.ok) {
      if (response.status === 401 && token) Auth.logout(true);
      const messages = { 401: "Please sign in to continue.", 403: "You do not have permission to do that.", 404: "The requested item could not be found.", 500: "The server could not complete the request." };
      throw new Error(data?.message || messages[response.status] || `Request failed (${response.status}).`);
    }
    return data;
  },
  get(path) { return this.request(path); },
  post(path, body) { return this.request(path, { method: "POST", body: JSON.stringify(body) }); },
  put(path, body) { return this.request(path, { method: "PUT", body: body === undefined ? undefined : JSON.stringify(body) }); }
};