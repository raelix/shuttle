/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogicgames.superjumper;

public class Squirrel extends DynamicGameObject {
	public static final float SQUIRREL_WIDTH = 1.5f;
	public static final float SQUIRREL_HEIGHT = 1.5f;
	public static final float SQUIRREL_VELOCITY = 3f;
	public static final float BUBBLE_CLISION=3;
	public static final float NOS_CLISION=2;
	public static final float LIFE_CLISION=1;
	public static final float PROJ_CLISION=4;
	public float stateTime = 0;
	public float state;
	public float crashtime = 0;
	public float nostime = 0;
	public boolean inuse= false;
	public boolean nosTap=false;
	public float bubbleuse=0;
	public Squirrel (float x, float y) {
		super(x, y, SQUIRREL_WIDTH, SQUIRREL_HEIGHT);
		//velocity.set(SQUIRREL_VELOCITY, 0);
	}

	public void update (float deltaTime) {
		//position.add(velocity.x * deltaTime, velocity.y * deltaTime);
		bounds.x = position.x ;
		bounds.y = position.y ;

		if (position.x < SQUIRREL_WIDTH / 2) {
		//	position.x = SQUIRREL_WIDTH / 2;
		//	velocity.x = SQUIRREL_VELOCITY;
		}
		if (position.x > World.WORLD_WIDTH - SQUIRREL_WIDTH / 2) {
		//	position.x = World.WORLD_WIDTH - SQUIRREL_WIDTH / 2;
		//	velocity.x = -SQUIRREL_VELOCITY;
		}
		
		if(state==Squirrel.BUBBLE_CLISION && crashtime != 0 && crashtime<stateTime-6){
			bubbleuse=0;
			
			inuse=false;
			
		}
		else if(state==Squirrel.NOS_CLISION && nostime != 0 && nostime<stateTime-6){

			
			inuse=false;
		}
		else if(state==Squirrel.LIFE_CLISION || state==Squirrel.PROJ_CLISION){
			
			inuse=false;
		}
		stateTime += deltaTime;
	}
}
