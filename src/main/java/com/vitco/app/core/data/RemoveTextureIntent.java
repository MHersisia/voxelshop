package com.vitco.app.core.data;

import javax.swing.ImageIcon;

import com.vitco.app.core.data.history.VoxelActionIntent;

final class RemoveTextureIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private ImageIcon texture;
    private final int textureId;

    protected RemoveTextureIntent(VoxelData voxelData, int textureId, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.textureId = textureId;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            if (textureId == this.voxelData.dataContainer.selectedTexture) {
                this.voxelData.historyManagerV.applyIntent(new SelectTextureIntent(this.voxelData, -1, true));
            }
        }
        texture = this.voxelData.dataContainer.textures.get(textureId);
        this.voxelData.dataContainer.textures.remove(textureId);
    }

    @Override
    protected void unapplyAction() {
        this.voxelData.dataContainer.textures.put(textureId, texture);
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