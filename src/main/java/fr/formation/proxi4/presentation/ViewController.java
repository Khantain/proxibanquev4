package fr.formation.proxi4.presentation;

import java.time.LocalDate;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.formation.proxi4.Proxi4Constants;
import fr.formation.proxi4.metier.entity.Survey;
import fr.formation.proxi4.metier.service.AnswerService;
import fr.formation.proxi4.metier.service.ClientService;
import fr.formation.proxi4.metier.service.SurveyService;

@Controller
@RequestMapping("/")
@Transactional(readOnly = true)
public class ViewController {

	private static final Logger LOGGER = Logger.getLogger(ViewController.class);

	@Autowired
	private SurveyService surveyService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private AnswerService answerService;

	@RequestMapping({ "", "index" })
	public ModelAndView index(@RequestParam(required = false) String message,
			@RequestParam(required = false) String closeMessage) {
		LOGGER.info("Entrée sur la page index.");
		LOGGER.info("Message récupéré suite à un redirection : " + message);
		System.out.println(closeMessage);
		ModelAndView mav = new ModelAndView();
		mav.setViewName("index");
		LOGGER.info("Ajout du sondage actuel.");
		mav.addObject("message", message);
		mav.addObject("closeMessage", closeMessage);
		Survey currentSurvey = this.surveyService.getCurrentSurvey();
		System.out.println(currentSurvey);
		mav.addObject("survey", currentSurvey);
		LOGGER.info("Sondage ajouté.");
		return mav;
	}

	@RequestMapping("surveys")
	public ModelAndView loadSurveys() {
		LOGGER.info("Entrée page des sondages.");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("surveys");
		LOGGER.info("Chargement des sondages");
		List<Survey> surveys = this.surveyService.readAll();
		Integer neg = this.surveyService.countNeg(surveys);
		Integer pos = this.surveyService.countPos(surveys);
		mav.addObject("surveys", surveys);
		mav.addObject("neg", neg);
		mav.addObject("pos", pos);
		return mav;
	}

	@RequestMapping("survey")
	public ModelAndView showSurvey(@RequestParam Integer id) {
		LOGGER.info("Entrée sur la page Sondage");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("survey");
		LOGGER.info("Lecture du sondage.");
		Survey survey = this.surveyService.read(id);
		LOGGER.info("Hibernate.initialize.");
		Hibernate.initialize(survey);
		LOGGER.info("Ajout du sondage au mav.");
		mav.addObject("survey", survey);
		return mav;
	}

	@RequestMapping("form")
	public ModelAndView createSurvey() {
		LOGGER.info("Entrée sur le formulaire de création du Sondage");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("form");
		mav.addObject("survey", new Survey());
		return mav;
	}

	@RequestMapping(path = "form", method = RequestMethod.POST)
	public String validateSurvey(String startDate, String tempEndDate, RedirectAttributes attributes) {
		LOGGER.info("Création du sondage en DB.");
		LOGGER.info(startDate + " " + tempEndDate);
		String message = null;
		LOGGER.info("Formatage des dates.");
		LocalDate sDate = this.surveyService.dateFormat(startDate);
		LocalDate eDate = this.surveyService.dateFormat(tempEndDate);
		LOGGER.info("Ajout du sondage.");
		this.surveyService.create(new Survey(sDate, eDate));
		message = "Sondage ajouté";
		attributes.addFlashAttribute("message", message);
		LOGGER.info("Renvoi du message");
		return Proxi4Constants.REDIRECT_TO_INDEX;
	}

	@RequestMapping("close")
	public String closeSurvey(@RequestParam Integer id, RedirectAttributes attributes) {
		LOGGER.info("Set du sondage pour lui ajouter une date de fin");
		Survey survey = this.surveyService.read(id);
		LOGGER.info("Hibernate.initialize.");
		Hibernate.initialize(survey);
		LOGGER.info("Setting de la date.");
		survey.setEndDate(LocalDate.now());
		LOGGER.info("Update du sondage.");
		this.surveyService.update(survey);
		String closeMessage = "Sondage terminé.";
		LOGGER.info("Fin de close survey" + closeMessage);
		attributes.addFlashAttribute("closeMessage", closeMessage);
		System.out.println(attributes.getFlashAttributes());
		return Proxi4Constants.REDIRECT_TO_INDEX;
	}
}
