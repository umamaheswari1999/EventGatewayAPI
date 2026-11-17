package com.example.EventGateway.repo;

import com.example.EventGateway.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventGatewayRepo extends JpaRepository<Event, String> {

    List<Event> findByAccountIdOrderByEventTimestampAsc(String accountId);

}
