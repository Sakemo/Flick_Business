package br.com.king.flick_business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.king.flick_business.entity.Provider;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
}