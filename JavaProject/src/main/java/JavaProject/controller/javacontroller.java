package JavaProject.controller;



import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import JavaProject.models.City;
import JavaProject.models.Driver;
import JavaProject.models.Trip;
import JavaProject.models.Users;
import JavaProject.services.CityService;
import JavaProject.services.DriverService;
import JavaProject.services.TripService;
import JavaProject.services.UsersService;
import JavaProject.valedator.DriverValidator;
import JavaProject.valedator.UserValidator;


@Controller
public class javacontroller {
	private final CityService cityService;
	private final DriverValidator  driverValidator;
	private final DriverService driverService;
	private final UsersService usersservice;
	private final UserValidator userValidator;
	private final TripService tripService;
	
	

	public javacontroller(CityService cityService, DriverValidator driverValidator, DriverService driverService,
			UsersService usersservice, UserValidator userValidator,TripService tripService) {
		super();
		this.cityService = cityService;
		this.driverValidator = driverValidator;
		this.driverService = driverService;
		this.usersservice = usersservice;
		this.userValidator = userValidator;
		this.tripService=tripService;
	}

	@RequestMapping("/")
	public String home() {
		cityService.createAllCity();
		return "fortest.jsp";
	}
	
	@RequestMapping("/userlog")
	public String user_Log(Model model,@ModelAttribute("userlog") Users user) {
		List<City> c=cityService.findAllCity();
		model.addAttribute("city",c);
		return "userlogin.jsp";
	}
	
	@RequestMapping("/loginasdriver")
	public String driver_Log(Model model) {
		
		List<City> c=cityService.findAllCity();
		model.addAttribute("driver",new Driver());
		model.addAttribute("city",c);
		return "driver.jsp";
	}
	
	@RequestMapping("/test")
	public String userhome(@ModelAttribute("trip") Trip trip,HttpSession session) {
		
	if(session.getAttribute("user") !=null) {
    		
		return "home.jsp";	
    			}else
    				return "redirect:/userlog";	
    		}
		
	
	
	
	@PostMapping(value="/loginasuser")
    public String registerdriver(@Valid @ModelAttribute("userlog") Users user,BindingResult result,HttpSession session, Model model) {
		userValidator.validate(user, result); 
		if(result.hasErrors()) {
		List<City> c=cityService.findAllCity();
		model.addAttribute("city",c);
		return "userlogin.jsp";
	}else{
		
		Users d = usersservice.registerUser(user);
		
    	session.setAttribute("user", d.getId());
    	return "redirect:/userlog";
	}
    	
    	}
	
	
    	
    	
    	
    	@PostMapping(value="/loginasdriver")
        public String registerUser(@Valid @ModelAttribute("driver") Driver driver, BindingResult result, HttpSession session, Model model) {
    		driverValidator.validate(driver, result); 
    		 
        	if(result.hasErrors()) {
        		List<City> c=cityService.findAllCity();
        		model.addAttribute("city",c);
        		return "driver.jsp";
        	}else{
        	Driver d = driverService.registerDriver(driver);
        	session.setAttribute("user", d.getId());
        	return "redirect:/driver";
        	}
    }

	 @PostMapping(value="/logdri")
	    public String loginUser(@RequestParam("email") String email, @ModelAttribute("driver") Driver driver, @RequestParam("password") String password, Model model, HttpSession session) {
	        boolean isAuthenticated = driverService.authenticateUser(email, password);
	        if(isAuthenticated) {
	        	Driver d =driverService.findByEmail(email); 
	        	session.setAttribute("user", d.getId() );
	        	return "redirect:/driver";
	        }else {
	        	model.addAttribute("error", "Invalid credentials. Try again!");
	       
	        	return "driver.jsp";
	        }
	    }

    	
	 @PostMapping("/login")
    	public String userLogIN(@RequestParam("email") String email,@RequestParam("password") String password, Model model, HttpSession session,@Valid @ModelAttribute("trip") Trip trip,BindingResult ros) {
    		boolean isAuthenticated = usersservice.authenticateUser(email, password);
    		if(isAuthenticated) {
    			Users u = usersservice.findByEmail(email);
    			session.setAttribute("user", u.getId());
    			return "redirect:/test";
    		}
    		else {
    			
    			model.addAttribute("error", "Invalid Credentials! Please try again with the correct user information!");
    			if(session.getAttribute("user") !=null) {
    		
			return "redirect:/test";	
    			}else
    				return "redirect:/userlog";	
    		}
    	}
    	
    	

    	
    	@PostMapping("/gettaxi")
    	public String getTaxi(@Valid @ModelAttribute("trip") Trip trip,BindingResult result,HttpSession session,Model model) {

    		System.out.println(session.getAttribute("user"));
    		if(session.getAttribute("user") !=null) {
    	
    		if(result.hasErrors()) {
    			System.out.println("iam in the errors :D");
    			return"redirect:/test";
    		}else {
    			

    		trip=tripService.createTrip(trip);
    		Long id=(Long)session.getAttribute("user");
    		usersservice.addTrip(trip,id);
    		
    		
    		
    		
    		return "redirect:/show";
    		}
    	}
    	else
    		return "redirect:/userlog";
    	}
    	
       	@RequestMapping("/driver")
    	public String Ta(@Valid @ModelAttribute("trip") Trip trip,Model model,HttpSession session) {
    		if(session.getAttribute("user") !=null) {
    			List<Trip> t=tripService.findAllTrip();
    			Driver d=driverService.findUserById((Long)session.getAttribute("user"));
    			
    			model.addAttribute("trips", t);
    			model.addAttribute("user", d);
    		return "home2.jsp";
    		
    	}
    	
    		return "redirect:/loginasdriver";
    	}
    	
    	
    	@RequestMapping("/logout")
    	public String logOut(HttpSession session) {
    		session.invalidate();
    		
    		return "redirect:/";
    	}
    	
    	@RequestMapping("/show")
    	public String show(HttpSession session,Model model) {
    		if(session.getAttribute("user") !=null) {
    			Long id=(Long)session.getAttribute("user");
        		Users u= usersservice.findUserById(id);
        		
    			model.addAttribute("trip", tripService.findByUserContains(u));
    			return "main.jsp";
    		}else
    			
    			return "redirect:/userlog";
    		
    	}
    	
    	@RequestMapping("/confirm/{id}")
    	public String Take(@PathVariable("id") Long id,Model model) {
    		Trip t=tripService.findById(id);
    		model.addAttribute("trip",t);
    		return "confimtaxi.jsp";

    		
    	}
    	
    
    	@PostMapping("/confirm/{id}/edit/{id1}")
    	public String confirm(@PathVariable("id") Long id,@PathVariable("id1") Long id2,@RequestParam("cost") int cost,@RequestParam("time") int time,HttpSession session,Model model) {
    		
    		Trip t=tripService.findById(id);
    		
    		t.setCost(cost);
    		t.setTime(time);
    		
    		Long id1=(Long)session.getAttribute("user");
    		driverService.updatetrip(t,id2);
    		
    		
    		
    		
    		return "redirect:/driver";
    		}	
    	
    	
    	@RequestMapping("/showdem/{id}")
    	public String toshow(@PathVariable("id") Long id,Model model) {
    		Trip t=tripService.findById(id);
    		model.addAttribute("show", t);
    		return "detail.jsp";
    	}
	

	

}
