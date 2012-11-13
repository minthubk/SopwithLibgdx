package net.tyler.sopwith

import com.badlogic.gdx.Screen

import net.tyler.math.ImmutableVector2f

class InGameScreen extends Screen {

  val querier = new InGameStateQuerier(new PlaneState(new ImmutableVector2f(Configuration.GAME_WIDTH / 2f, Configuration.GAME_HEIGHT / 2f),
                                                      new ImmutableVector2f(0f, 0f),
                                                      0f, 0f),
                                       List(new Building(new ImmutableVector2f(Configuration.GAME_WIDTH / 2f, 0f))))
  val renderer = new InGameRenderer(querier)
  
  def render(dt: Float) {
    renderer.renderLevel
  }
  
  def show() = {}
  
  def hide() = {}
  
  def resize(width: Int, height: Int) = {}
  
  def pause() = {}
  
  def resume() = {}
  
  def dispose() = {} 
}