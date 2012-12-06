package net.tyler.sopwith

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Peripheral
import net.tyler.messaging.MessagePassing
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Input.Keys

/**
 * Class is responsible for handling input device polling and converting the
 * results into state change messages to pass back to the game model.
 */
class InGameInputProcessor(private val querier: InGameStateQuerier, 
                           private val messagePassing: MessagePassing) extends InputProcessor {

  /**
   * Called once per render loop to process any new input and convert it into
   * game state messages.
   * 
   * Accelerometer input is only available via polling (rather than event 
   * based).
   */
  def processInput {    
    /*
     * Control of the plane is either done via the accelerometer (when 
     * available) or via the keyboard when the accelerometer is not available.
     */
    if (Gdx.input.isPeripheralAvailable(Peripheral.Accelerometer)) {
      processAccelerometerInput(TimeUtils.millis)
    }
  }
  
  private def processAccelerometerInput(t: Long) {
    
  }
  
  override def keyDown(keyCode: Int) = keyCode match {
    case Keys.UP | Keys.DOWN => {
      messagePassing.send(new PlaneOrientationFlip(TimeUtils.millis))
      
      true
    }
    case Keys.LEFT => {
      true
    }
    case Keys.RIGHT => {
      true
    }
  }
  
  override def keyTyped(char: Char) = { false }
  
  override def keyUp(keyCode: Int) = { false }
  
  override def scrolled(amount: Int) = { false }
  
  override def touchDown(x: Int, y: Int, pointer: Int, button: Int) = true
  
  /**
   * Touch based input (includes mouse pointer input) is used to interact with
   * the planes weapons.
   */
  override def touchUp(x: Int, y: Int, pointer: Int, button: Int) = {
    val t = TimeUtils.millis
    
    /*
     * TODO - DAT - Should add in check to make sure that the touch is near
     * the plane. Blocked on collision code probably.
     */
    if (querier.bombsRemaining(t) > 0) {
      messagePassing.send(new BombReleased(querier.planeState(t).position, t))
    }
    
    /*
     * Flag that we have processed this message (not really necessary).
     */
    true
  }
  
  override def touchDragged(x: Int, y: Int, pointer: Int) = { false }
  
  override def mouseMoved(x: Int, y: Int) = { false }
}