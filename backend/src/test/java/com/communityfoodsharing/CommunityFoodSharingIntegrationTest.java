package com.communityfoodsharing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommunityFoodSharingIntegrationTest {
    private static final String JWT_SECRET = "test-only-jwt-secret-with-more-than-32-bytes";
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @Test
    void registrationAuthorizationAndDonationLifecycleWorkEndToEnd() throws Exception {
        register("Donor One", "donor@example.test", "DONOR").andExpect(status().isCreated());
        register("Volunteer One", "volunteer@example.test", "VOLUNTEER").andExpect(status().isCreated());
        register("Volunteer Two", "volunteer2@example.test", "VOLUNTEER").andExpect(status().isCreated());
        register("Other Donor", "other@example.test", "DONOR").andExpect(status().isCreated());
        register("Admin Hacker", "hacker@example.test", "ADMIN").andExpect(status().isForbidden());
        register("Donor One", "donor@example.test", "DONOR").andExpect(status().isConflict());

        String donor = login("donor@example.test", "password-123");
        String otherDonor = login("other@example.test", "password-123");
        String volunteer = login("volunteer@example.test", "password-123");
        String secondVolunteer = login("volunteer2@example.test", "password-123");
        String admin = login("admin@foodshare.test", "test-admin-password-123");

        mvc.perform(get("/api/donor/dashboard")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/donor/dashboard").header("Authorization", "Bearer not-a-jwt")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/donor/dashboard").header("Authorization", "Bearer " + expiredToken())).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/dashboard").header("Authorization", "Bearer " + donor)).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/dashboard").header("Authorization", "Bearer " + volunteer)).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(Map.of("email", "donor@example.test", "password", "wrong-password"))))
            .andExpect(status().isUnauthorized());

        Map<String, Object> donation = Map.ofEntries(
            Map.entry("foodName", "Vegetable lunch boxes"), Map.entry("description", "Fresh sealed lunch boxes prepared today"),
            Map.entry("quantity", 12), Map.entry("quantityUnit", "meals"), Map.entry("foodType", "VEGETARIAN"),
            Map.entry("expiryTime", LocalDateTime.now().plusDays(2).withNano(0).toString()),
            Map.entry("pickupAddress", "12 Market Street"), Map.entry("city", "Springfield"),
            Map.entry("pickupDate", LocalDate.now().plusDays(1).toString()), Map.entry("pickupTime", LocalTime.of(15, 30).toString()),
            Map.entry("additionalInstructions", "Use the side entrance")
        );
        JsonNode created = mapper.readTree(request(post("/api/donor/donations"), donation, donor).andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString());
        long donationId = created.path("id").asLong();
        assertEquals("AVAILABLE", created.path("status").asText());
        mvc.perform(get("/api/donor/donations/" + donationId).header("Authorization", "Bearer " + otherDonor))
            .andExpect(status().isNotFound());

        JsonNode available = mapper.readTree(mvc.perform(get("/api/volunteer/donations/available").header("Authorization", "Bearer " + volunteer))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals(1, available.size());
        assertTrue(available.get(0).path("donorPhone").isNull());
        JsonNode accepted = mapper.readTree(request(put("/api/volunteer/donations/" + donationId + "/accept"), null, volunteer)
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals("ACCEPTED", accepted.path("status").asText());
        request(put("/api/volunteer/donations/" + donationId + "/accept"), null, secondVolunteer).andExpect(status().isConflict());
        request(put("/api/volunteer/donations/" + donationId + "/complete"), null, volunteer).andExpect(status().isConflict());
        request(put("/api/volunteer/donations/" + donationId + "/pickup"), null, secondVolunteer).andExpect(status().isForbidden());

        JsonNode pickedUp = mapper.readTree(request(put("/api/volunteer/donations/" + donationId + "/pickup"), null, volunteer)
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals("PICKED_UP", pickedUp.path("status").asText());
        Map<String, Object> distribution = Map.of("distributionLocation", "Community Hall", "organizationName", "Northside Shelter",
            "peopleServed", 18, "distributionNotes", "Meals served at the evening program");
        JsonNode distributed = mapper.readTree(request(put("/api/volunteer/donations/" + donationId + "/distribute"), distribution, volunteer)
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals("DISTRIBUTED", distributed.path("status").asText());
        assertEquals(18, distributed.path("peopleServed").asInt());
        JsonNode completed = mapper.readTree(request(put("/api/volunteer/donations/" + donationId + "/complete"), null, volunteer)
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals("COMPLETED", completed.path("status").asText());

        JsonNode donorView = mapper.readTree(mvc.perform(get("/api/donor/donations/" + donationId).header("Authorization", "Bearer " + donor))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals("COMPLETED", donorView.path("status").asText());
        JsonNode adminDashboard = mapper.readTree(mvc.perform(get("/api/admin/dashboard").header("Authorization", "Bearer " + admin))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals(1, adminDashboard.path("completedDonations").asInt());
        assertEquals(18, adminDashboard.path("peopleServed").asInt());
        mvc.perform(get("/api/admin/donations?status=COMPLETED").header("Authorization", "Bearer " + admin))
            .andExpect(status().isOk());
        JsonNode adminDonation = mapper.readTree(mvc.perform(get("/api/admin/donations/" + donationId).header("Authorization", "Bearer " + admin))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals("Northside Shelter", adminDonation.path("organizationName").asText());

        JsonNode notifications = mapper.readTree(mvc.perform(get("/api/notifications").header("Authorization", "Bearer " + donor))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertTrue(notifications.size() >= 4);
        long notificationId = notifications.get(0).path("id").asLong();
        mvc.perform(put("/api/notifications/" + notificationId + "/read").header("Authorization", "Bearer " + otherDonor))
            .andExpect(status().isNotFound());

        long competingDonationId = mapper.readTree(request(post("/api/donor/donations"), donation, donor)
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).path("id").asLong();
        var executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Integer> first = executor.submit(() -> {
                start.await();
                return request(put("/api/volunteer/donations/" + competingDonationId + "/accept"), null, volunteer).andReturn().getResponse().getStatus();
            });
            Future<Integer> second = executor.submit(() -> {
                start.await();
                return request(put("/api/volunteer/donations/" + competingDonationId + "/accept"), null, secondVolunteer).andReturn().getResponse().getStatus();
            });
            start.countDown();
            int firstStatus = first.get(); int secondStatus = second.get();
            assertTrue((firstStatus == 200 && secondStatus == 409) || (firstStatus == 409 && secondStatus == 200));
        } finally {
            executor.shutdownNow();
        }
    }

    private org.springframework.test.web.servlet.ResultActions register(String name, String email, String role) throws Exception {
        return request(post("/api/auth/register"), Map.of("fullName", name, "email", email, "phone", "+15551234567",
            "password", "password-123", "role", role), null);
    }
    private String login(String email, String password) throws Exception {
        JsonNode response = mapper.readTree(request(post("/api/auth/login"), Map.of("email", email, "password", password), null)
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        return response.path("token").asText();
    }
    private org.springframework.test.web.servlet.ResultActions request(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder,
        Object body, String token) throws Exception {
        if (body != null) builder.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return mvc.perform(builder);
    }
    private String expiredToken() {
        Date expiredAt = Date.from(Instant.now().minusSeconds(300));
        return Jwts.builder().subject("donor@example.test").issuedAt(expiredAt).expiration(expiredAt)
            .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8))).compact();
    }
}