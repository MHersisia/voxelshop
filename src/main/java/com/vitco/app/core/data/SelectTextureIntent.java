package com.vitco.app.core.data;

import com.vitco.app.core.data.history.VoxelActionIntent;

// select texture in the texture list
final class SelectTextureIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final int oldTextureId;
    private final int newTextureId;

    protected SelectTextureIntent(VoxelData voxelData, Integer textureId, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.newTextureId = textureId;
        oldTextureId = this.voxelData.dataContainer.selectedTexture;
    }

    @Override
    protected void applyAction() {
        this.voxelData.dataContainer.selectedTexture = newTextureId;
    }

    @Override
    protected void unapplyAction() {
        this.voxelData.dataContainer.selectedTexture = oldTextureId;
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