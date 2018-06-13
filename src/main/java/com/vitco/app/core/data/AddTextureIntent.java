package com.vitco.app.core.data;

import javax.swing.ImageIcon;

import com.vitco.app.core.data.history.VoxelActionIntent;

// texture intents
final class AddTextureIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final ImageIcon texture;
    private final int textureId;

    protected AddTextureIntent(VoxelData voxelData, ImageIcon texture, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.texture = texture;
        textureId = this.voxelData.getFreeTextureId();
    }

    @Override
    protected void applyAction() {
        this.voxelData.dataContainer.textures.put(textureId, texture);
        if (isFirstCall()) {
            this.voxelData.historyManagerV.applyIntent(new SelectTextureIntent(this.voxelData, textureId, true));
        }
    }

    @Override
    protected void unapplyAction() {
        this.voxelData.dataContainer.textures.remove(textureId);
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