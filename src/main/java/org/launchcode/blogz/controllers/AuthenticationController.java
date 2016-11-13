package org.launchcode.blogz.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.launchcode.blogz.models.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
public class AuthenticationController extends AbstractController {

	static class AuthenticationError {
		private String[] userError = {"Invalid Username.", "Username already exists.", "Could not find a user by that username."};
		private String[] passwordError = {"Please enter a valid password", "Incorrect password."};
		private String confirmError = "Passwords do not match.";
	}

	@RequestMapping(value = "/signup", method = RequestMethod.GET)
	public String signupForm() {
		return "signup";
	}

	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public String signup(HttpServletRequest request, Model model) {

		AuthenticationError error = new AuthenticationError();		

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String verify = request.getParameter("verify");


		//Validate data.
		boolean hasError = false;
		if (!User.isValidUsername(username)) {
			hasError = true;
			model.addAttribute("username_error", error.userError[0]);
		}
		else { // username is valid

			// if this username already exists
			if (userDao.findByUsername(username) != null) {					//Should this test be != null or !.equals("")?
				model.addAttribute("username_error", error.userError[1]);
				hasError = true;
			}
			
			// username doesn't already exist
			else {
				
				if (!User.isValidPassword(password)) {
					model.addAttribute("password_error", error.passwordError[0]);
					hasError = true;
				}
				model.addAttribute("username", username);
				//here - user doesn't exist & password is valid
			}
		}
		if (!verify.equals(password)) {
			model.addAttribute("verify_error", error.confirmError);
			hasError = true;
		}


//		System.out.println("\n\nhasError is " + hasError + "\n");

		if (hasError) {
			return "signup";
		}

		// create user/ create session / put them in
		User user = new User(username, password);
		userDao.save(user);
		login(request, user);

		return "redirect:blog/newpost";
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String loginForm() {
		return "login";
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String login(HttpServletRequest request, Model model) {

		AuthenticationError error = new AuthenticationError();

		String username = request.getParameter("username");
		String password = request.getParameter("password");

		User user = userDao.findByUsername(username);

		boolean hasError = false;
		if (user == null) {					//if userDao,findBy...fails, does it return null?-YES  What gets returned?  ANSWERED: null
			model.addAttribute("error", error.userError[2]);
			hasError = true;
		}
		else {
			model.addAttribute("username", user.getUsername());
			if (!user.isMatchingPassword(password)) {
				model.addAttribute("error", error.passwordError[1]);
				hasError = true;
			}
		}

		if (hasError) {
			return "login";
		}


		login(request, user);

		return "redirect:blog/newpost";
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logout(HttpServletRequest request){
		request.getSession().invalidate();
		return "redirect:/";
	}


	private void login(HttpServletRequest request, User user) {
		HttpSession newSession = request.getSession();
		setUserInSession(newSession, user);
	}
}
