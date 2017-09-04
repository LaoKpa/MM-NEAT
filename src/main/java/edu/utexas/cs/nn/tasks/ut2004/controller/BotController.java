package edu.utexas.cs.nn.tasks.ut2004.controller;

import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import edu.utexas.cs.nn.tasks.ut2004.actions.BotAction;

/**
 *
 * @author Jacob Schrum
 */
public interface BotController {

	public BotAction control(UT2004BotModuleController bot);

	public void initialize(UT2004BotModuleController bot);

	public void reset(UT2004BotModuleController bot);
}
