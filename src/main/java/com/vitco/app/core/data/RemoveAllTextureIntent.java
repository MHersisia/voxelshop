package com.vitco.app.core.data;

import java.util.ArrayList;

import com.vitco.app.core.data.history.VoxelActionIntent;

// clear the texture list, remove unused texture
final class RemoveAllTextureIntent extends VoxelActionIntent {

    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final ArrayList<Integer> unusedTextures;

    protected RemoveAllTextureIntent(VoxelData voxelData, ArrayList<Integer> unusedTextures, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.unusedTextures = unusedTextures;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            // deselect texture
            if (unusedTextures.contains(this.voxelData.dataContainer.selectedTexture)) {
                this.voxelData.historyManagerV.applyIntent(new SelectTextureIntent(this.voxelData, -1, true));
            }
            // remove the textures
            for (int i : unusedTextures) {
                this.voxelData.historyManagerV.applyIntent(new RemoveTextureIntent(this.voxelData, i, true));
            }
            // we don't need this data anymore
            unusedTextures.clear();
        }
    }

    @Override
    protected void unapplyAction() {
        // nothing to do here
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