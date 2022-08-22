package controllers;

import play.mvc.Result;

import static play.mvc.Results.ok;

public class HealthcheckController {

    public Result healthcheck() {
        return ok("AWB Generator is up");
    }
}
