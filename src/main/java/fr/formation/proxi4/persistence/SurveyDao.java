package fr.formation.proxi4.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.formation.proxi4.metier.entity.Survey;

@Repository
public interface SurveyDao extends JpaRepository<Survey, Integer> {

}
