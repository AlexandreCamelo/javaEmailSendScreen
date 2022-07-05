package br.com.tudodev.envioemails.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.tudodev.envioemails.modelos.EmailAutoComplete;

@Repository
public interface EmailAutoCompleteRepository extends JpaRepository<EmailAutoComplete, Integer> {


}
