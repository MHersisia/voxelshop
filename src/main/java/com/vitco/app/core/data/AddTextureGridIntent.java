package com.vitco.app.core.data;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import com.vitco.app.core.data.history.VoxelActionIntent;

// texture intents
final class AddTextureGridIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final BufferedImage texture;

    protected AddTextureGridIntent(VoxelData voxelData, BufferedImage texture, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.texture = texture;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            // split the image and add
            for (int y = 0, lenY = texture.getHeight(); y < lenY; y+= 32) {
                for (int x = 0, lenX = texture.getWidth(); x < lenX; x+= 32) {
                    this.voxelData.historyManagerV.applyIntent(
                            new AddTextureIntent(
                                    this.voxelData, new ImageIcon(texture.getSubimage(x, y, 32, 32)), true)
                    );
                }
            }
        }
    }

    @Override
    protected void unapplyAction() {
        // nothing to do
    }

    @Override
    public int[][] effected() {
        // nothing effected
        return new int[0][];
    }

    // return true if this action effects textures
    public boolean effectsTexture() {
        return true;
    }
}