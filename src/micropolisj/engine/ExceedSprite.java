// This file is part of MicropolisJ.
// Copyright (C) 2013 Jason Long
// Portions Copyright (C) 1989-2007 Electronic Arts Inc.
//
// MicropolisJ is free software; you can redistribute it and/or modify
// it under the terms of the GNU GPLv3, with additional terms.
// See the README file, included in this distribution, for details.

package micropolisj.engine;

import static micropolisj.engine.TileConstants.*;

/**
 * Implements an Exceed (cat with wings that reduces traffic)
 */
public class ExceedSprite extends Sprite
{
	int count;
	int soundCount;
	int destX;
	int destY;
	int origX;
	int origY;
	int step;
	boolean flag; //true if the monster wants to return home

	//GODZILLA FRAMES
	//   1...3 : northeast
	//   4...6 : southeast
	//   7...9 : southwest
	//  10..12 : northwest
	//      13 : north
	//      14 : east
	//      15 : south
	//      16 : west

	// movement deltas
	static int [] Gx = { 2, 2, -2, -2, 0 };
	static int [] Gy = { -2, 2, 2, -2, 0 };

	static int [] ND1 = {  0, 1, 2, 3 };
	static int [] ND2 = {  1, 2, 3, 0 };
	static int [] nn1 = {  2, 5, 8, 11 };
	static int [] nn2 = { 11, 2, 5,  8 };

	public ExceedSprite(Micropolis engine, int xpos, int ypos)
	{
		super(engine, SpriteKind.EXC);
		this.x = xpos * 16 + 8;
		this.y = ypos * 16 + 8;
		this.width = 48;
		this.height = 48;
		this.offx = -24;
		this.offy = -24;

		this.origX = x;
		this.origY = y;

		this.frame = xpos > city.getWidth() / 2 ?
					(ypos > city.getHeight() / 2 ? 10 : 7) :
					(ypos > city.getHeight() / 2 ? 1 : 4);

		this.count = 1000;
		CityLocation p = city.getLocationOfMaxTraffic();
		this.destX = p.x * 16 + 8;
		this.destY = p.y * 16 + 8;
		this.flag = false;
		this.step = 1;
	}

	public void moveImpl()
	{
		if (this.frame == 0) {
			return;
		}

		if (soundCount > 0) {
			soundCount--;
		}

		int d = (this.frame - 1) / 3;   // basic direction
		int z = (this.frame - 1) % 3;   // step index (only valid for d<4)

		if (d < 4) { //turn n s e w
			assert step == -1 || step == 1;
			if (z == 2) step = -1;
			if (z == 0) step = 1;
			z += step;

			if (getDis(x, y, destX, destY) < 60) {

				// reached destination

				if (!flag) {
//					System.out.println("Max traffic: (" + 
//										this.destX +
//										", " +
//										this.destY +
//										")");
					
					// destination was the previous max traffic area
					// update max traffic area
					CityLocation p = city.getLocationOfMaxTraffic();
					this.destX = p.x * 16 + 8;
					this.destY = p.y * 16 + 8;
					
					// no max traffic area found
					// remove Exceed
					if (this.destX == 8 && this.destY == 8)
					{
						flag = true;
						destX = origX;
						destY = origY;
					}
				}
				else {
					// destination was origX, origY
					// hide the sprite
					this.frame = 0; // remove Exceed
					return;
				}
			}

			int c = getDir(x, y, destX, destY);
			c = (c - 1) / 2;   //convert to one of four basic headings
			assert c >= 0 && c < 4;

			if ((c != d) && city.PRNG.nextInt(11) == 0) {
				// randomly determine direction to turn
				if (city.PRNG.nextInt(2) == 0) {
					z = ND1[d];
				}
				else {
					z = ND2[d];
				}
				d = 4;  //transition heading

				if (soundCount == 0) {
					city.makeSound(x/16, y/16, Sound.EXCEED);
					soundCount = 50 + city.PRNG.nextInt(101);
				}
			}
		}
		else {
			assert this.frame >= 13 && this.frame <= 16;

			int z2 = (this.frame - 13) % 4;

			if (city.PRNG.nextInt(4) == 0) {
				int newFrame;
				if (city.PRNG.nextInt(2) == 0) {
					newFrame = nn1[z2];
				} else {
					newFrame = nn2[z2];
				}
				d = (newFrame-1) / 3;
				z = (newFrame-1) % 3;

				assert d < 4;
			}
			else {
				d = 4;
			}
		}

		this.frame = ((d * 3) + z) + 1;

		assert this.frame >= 1 && this.frame <= 16;

		this.x += Gx[d];
		this.y += Gy[d];

		if (this.count > 0) {
			this.count--;
		}
	}
}