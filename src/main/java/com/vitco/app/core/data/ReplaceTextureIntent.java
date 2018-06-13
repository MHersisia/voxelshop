package com.vitco.app.core.data;

import javax.swing.ImageIcon;

import com.vitco.app.core.data.history.VoxelActionIntent;

// replace texture in the texture list
final class ReplaceTextureIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final ImageIcon textureNew;
    private final ImageIcon textureOld;
    private final int textureId;

    protected ReplaceTextureIntent(VoxelData voxelData, int textureId, ImageIcon texture, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.textureId = textureId;
        this.textureNew = texture;
        this.textureOld = this.voxelData.dataContainer.textures.get(textureId);
    }

    @Override
    protected void applyAction() {
        this.voxelData.dataContainer.textures.put(textureId, textureNew);
    }

    @Override
    protected void unapplyAction() {
        this.voxelData.dataContainer.textures.put(textureId, textureOld);
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