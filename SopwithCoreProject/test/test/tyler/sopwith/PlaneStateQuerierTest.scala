package test.tyler.sopwith

import org.junit.Assert._
import org.junit.Test
import net.tyler.math.CartesianVector2f
import net.tyler.messaging.MessagePassing
import net.tyler.messaging.MessagingComponent
import net.tyler.sopwith.ingame.BombDestroyed
import net.tyler.sopwith.ingame.BombReleased
import net.tyler.sopwith.ingame.BuildingDestroyed
import net.tyler.sopwith.ingame.InGameStateQuerier
import net.tyler.sopwith.ingame.PlaneOrientationFlip
import net.tyler.sopwith.ingame.PlaneOrientationFlip
import net.tyler.sopwith.ingame.PlaneState
import net.tyler.sopwith.ingame.PlaneVelocityChange
import net.tyler.sopwith.ingame.PlaneVelocityChange
import net.tyler.sopwith.ingame.PlaneAccelerationChange
import net.tyler.sopwith.ingame.PlaneAccelerationChange
import net.tyler.sopwith.ingame.PlanePositionChange

/**
 * Test class for checking that the plane updates it's state correctly under
 * various message circumstances.
 */
class PlaneStateQuerierTest {

  private val FP_DELTA = 0.01
  
  val initialPlaneState = new PlaneState(new CartesianVector2f(10f, 10f), new CartesianVector2f(1f, 2f), new CartesianVector2f(0f, 0f), false)
  
  trait StateTester {
    private val inGameMessageTypes = List(classOf[PlaneAccelerationChange],
                                          classOf[PlaneVelocityChange],
                                          classOf[PlanePositionChange],
                                          classOf[PlaneOrientationFlip],
                                          classOf[BombDestroyed],
                                          classOf[BombReleased],
                                          classOf[BuildingDestroyed])
    val messagingComponent = new MessagingComponent(inGameMessageTypes)
    
    val querier = new InGameStateQuerier(initialPlaneState, List(), 5, 0, messagingComponent)
  }
  
  @Test def initialState() {
    
    new ApplicationTester with StateTester {
      assertEquals(initialPlaneState, querier.planeState(0))
      assertEquals(initialPlaneState.velocity, querier.planeState(0).velocity)
      assertEquals(initialPlaneState.acceleration, querier.planeState(10).acceleration)
    }
  }
  
  @Test def singleVelocityChange() {
    new ApplicationTester with StateTester {
      val newVelocity = new CartesianVector2f(-1f, -0.5f)
      messagingComponent.send(new PlaneVelocityChange(newVelocity, 10))
      
      assertEquals(initialPlaneState.velocity, querier.planeState(9).velocity)
      assertEquals(newVelocity, querier.planeState(11).velocity)
    }
  }
  
  @Test def velocityChanges() {
    new ApplicationTester with StateTester {
      messagingComponent.send(new PlaneAccelerationChange(new CartesianVector2f(10f, 0f), 10))
      messagingComponent.send(new PlaneAccelerationChange(new CartesianVector2f(5f, 5f), 1010))
      
      assertEquals(initialPlaneState.velocity, querier.planeState(10).velocity)
      assertEquals(initialPlaneState.velocity.x + 10f, querier.planeState(1010).velocity.x, FP_DELTA)
      assertEquals(initialPlaneState.velocity.y, querier.planeState(1010).velocity.y, FP_DELTA)
      
      assertEquals(initialPlaneState.velocity.x + 10f + 5f, querier.planeState(2010).velocity.x, FP_DELTA)
      assertEquals(initialPlaneState.velocity.y + 5f, querier.planeState(2010).velocity.y, FP_DELTA)
      
      messagingComponent.send(new PlaneVelocityChange(new CartesianVector2f(7f, 7f), 90))
      
      assertEquals(7f + 1f/100f, querier.planeState(91).velocity.x, FP_DELTA)
      assertEquals(7f, querier.planeState(91).velocity.y, FP_DELTA)
    }
  }
  
  @Test def positionChangeProgression() {
    new ApplicationTester with StateTester {
      val newVelocity = new CartesianVector2f(-10f, 7f)
      messagingComponent.send(new PlaneVelocityChange(newVelocity, 10))
      
      assertEquals(initialPlaneState.position.x + initialPlaneState.velocity.x * 0.009f, querier.planeState(9).position.x, FP_DELTA)
      assertEquals(initialPlaneState.position.y + initialPlaneState.velocity.y * 0.009f, querier.planeState(9).position.y, FP_DELTA)
      
      val positionAfter1000ms = querier.planeState(1010).position
      assertEquals((initialPlaneState.position.x + initialPlaneState.velocity.x * 0.01f) + newVelocity.x, positionAfter1000ms.x, FP_DELTA)
      assertEquals((initialPlaneState.position.y + initialPlaneState.velocity.y * 0.01f) + newVelocity.y, positionAfter1000ms.y, FP_DELTA)
    }
  }
  
  @Test def planeOrientation() {
    new ApplicationTester with StateTester {
      messagingComponent.send(new PlaneOrientationFlip(10))
      
      assertEquals(false, querier.planeState(9).flipped)
      assertEquals(true, querier.planeState(11).flipped)
    }
  }
  
  @Test def planeRotation() {
    new ApplicationTester with StateTester {
      messagingComponent.send(new PlaneVelocityChange(new CartesianVector2f(0f, 1f), 9))
      messagingComponent.send(new PlaneVelocityChange(new CartesianVector2f(1f, 1f), 10))
      messagingComponent.send(new PlaneVelocityChange(new CartesianVector2f(1f, 0f), 11))
      messagingComponent.send(new PlaneVelocityChange(new CartesianVector2f(1f, -1f), 12))
      messagingComponent.send(new PlaneVelocityChange(new CartesianVector2f(0f, -1f), 13))
      messagingComponent.send(new PlaneVelocityChange(new CartesianVector2f(-1f, -1f), 14))
      messagingComponent.send(new PlaneVelocityChange(new CartesianVector2f(-1f, 0f), 15))
      messagingComponent.send(new PlaneVelocityChange(new CartesianVector2f(-1f, 1f), 16))
      
      assertEquals(90f, querier.planeState(10).angle, FP_DELTA)
      assertEquals(45f, querier.planeState(11).angle, FP_DELTA)
      assertEquals(0f, querier.planeState(12).angle, FP_DELTA)
      assertEquals(-45f, querier.planeState(13).angle, FP_DELTA)
      assertEquals(-90f, querier.planeState(14).angle, FP_DELTA)
      assertEquals(-135f, querier.planeState(15).angle, FP_DELTA)
      assertEquals(180f, querier.planeState(16).angle, FP_DELTA)
      assertEquals(135f, querier.planeState(17).angle, FP_DELTA)
    }
  }
}