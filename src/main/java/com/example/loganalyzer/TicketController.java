package com.example.loganalyzer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TicketController {

    private final SupportTicketRepository ticketRepository;

    public TicketController(SupportTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @PostMapping("/tickets")
    public ResponseEntity<Map<String, Object>> createTicket(@RequestBody SupportTicket ticket) {
        Map<String, Object> response = new HashMap<>();

        if (ticket.getName() == null || ticket.getName().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "Name is required.");
            return ResponseEntity.badRequest().body(response);
        }

        if (ticket.getEmail() == null || ticket.getEmail().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "Email is required.");
            return ResponseEntity.badRequest().body(response);
        }

        if (ticket.getMessage() == null || ticket.getMessage().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "Message is required.");
            return ResponseEntity.badRequest().body(response);
        }

        ticket.setName(ticket.getName().trim());
        ticket.setEmail(ticket.getEmail().trim());
        ticket.setMessage(ticket.getMessage().trim());

        SupportTicket savedTicket = ticketRepository.save(ticket);

        response.put("success", true);
        response.put("message", "Thank you! Your support ticket (#" + savedTicket.getId() + ") has been submitted successfully.");
        response.put("id", savedTicket.getId());
        response.put("name", savedTicket.getName());
        response.put("email", savedTicket.getEmail());
        response.put("createdAt", savedTicket.getCreatedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tickets")
    public List<SupportTicket> getAllTickets() {
        return ticketRepository.findAll();
    }
}
