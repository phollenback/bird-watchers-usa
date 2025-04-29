package com.birds.bird_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.birds.bird_app.model.GroupEntity;
import com.birds.bird_app.model.GroupMember;
import com.birds.bird_app.model.UserEntity;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    
    List<GroupMember> findByUser(UserEntity user);
    
    List<GroupMember> findByGroup(GroupEntity group);
    
    Optional<GroupMember> findByUserAndGroup(UserEntity user, GroupEntity group);
    
    boolean existsByUserAndGroup(UserEntity user, GroupEntity group);
    
    void deleteByUserAndGroup(UserEntity user, GroupEntity group);
} 