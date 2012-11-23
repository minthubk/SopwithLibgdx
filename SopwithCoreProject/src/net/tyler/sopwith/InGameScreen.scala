package net.tyler.sopwith

import com.badlogic.gdx.Screen
import net.tyler.math.ImmutableVector2f
import com.badlogic.gdx.utils.TimeUtils
import net.tyler.messaging.MessagingComponent
import net.tyler.messaging.Message
import net.tyler.messaging.MessagePassing
import com.badlogic.gdx.Gdx
import net.tyler.math.Vector2fConstants

class InGameScreen extends Screen {
  
  private val initialPlaneState = new PlaneState(Vector2fConstants.zero,
                                                 Vector2fConstants.zero,
                                                 new ImmutableVector2f(Configuration.GAME_WIDTH / 2f, Configuration.GAME_HEIGHT / 2f),
                                                 0f, 0f, 0f, false)

  private val inGameMessageTypes = List(classOf[PlaneAccelerationChange],
                                        classOf[PlaneVelocityChange],
                                        classOf[PlanePositionChange],
                                        classOf[PlaneAngularAccelerationChange],
                                        classOf[PlaneAngularVelocityChange],
                                        classOf[PlaneAngleChange],
                                        classOf[PlaneOrientationFlip],
                                        classOf[BombDestroyed],
                                        classOf[BombReleased],
                                        classOf[BuildingDestroyed])
  private val messagePassing = new MessagePassing
  private val messagingComponent = new MessagingComponent(messagePassing, inGameMessageTypes)
  private val querier = new InGameStateQuerier(initialPlaneState,
                                               List(new Building(new ImmutableVector2f(Configuration.GAME_WIDTH / 2f, 0f))),
                                               Configuration.INIT_BOMBS,
                                               TimeUtils.millis,
                                               messagingComponent)
  private val renderer = new InGameRenderer(querier)
  private val inputProcessor = new InGameInputProcessor(querier, messagePassing)
  private val objectStateUpdater = new InGameObjectChecker(querier, messagePassing) 
  
  def render(dt: Float) {
    /*
     * Handle changes to the game state based on polling user input. Event based
     * input is handled internally by libgdx (i.e. all mouse/key events).
     */
    inputProcessor.processInput
    
    /*
     * Handle changes to the game state based on collisions.
     */
    objectStateUpdater.checkObjectLiveness
    
    /*
     * Render the level based on the current game state.
     */
    renderer.renderLevel
  }
  
  def show() = {
    /*
     * Event based input processing goes through this. Polling is done for other
     * types and is done in the render/update loop.
     */
    Gdx.input.setInputProcessor(inputProcessor)
    
    messagePassing.send(new PlaneVelocityChange(new ImmutableVector2f(20f, -10f), TimeUtils.millis))
  }
  
  def hide() = {}
  
  def resize(width: Int, height: Int) = {}
  
  def pause() = {}
  
  def resume() = {}
  
  def dispose() = {} 
}