package com.youtube.jwt.dao;


import com.youtube.jwt.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageDao extends JpaRepository<Image, Long> {
	Optional<Image> findByName(String name);
}
