package com.example.GATEPath.Repository;

import com.example.GATEPath.Entity.MockTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockTestRepository extends JpaRepository<MockTest, Long> {

    List<MockTest> findByActiveTrue();

}