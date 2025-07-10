package br.com.king.flick_business.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.king.flick_business.entity.GeneralSettings;

@Repository
public interface GeneralSettingsRepository extends JpaRepository<GeneralSettings, Long> {
  default Optional<GeneralSettings> findConfig() {
    return findById(1L);
  }
}