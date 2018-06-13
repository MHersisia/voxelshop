package com.vitco.app.importer;

import com.vitco.app.util.file.FileIn;
import com.vitco.app.util.file.RandomAccessFileIn;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * *.qb importer
 */
public class QbImporter extends AbstractImporter {

    // constructor
    public QbImporter(File file, String name) throws IOException {
        super(file, name);
    }

    private static final int CODE_FLAG = 2;
    private static final int NEXT_SLICE_FLAG = 6;

    @Override
    protected boolean read(FileIn fileIn, RandomAccessFileIn raf) throws IOException {
        fileIn.readIntRevUnsigned();

        int colorFormat = fileIn.readIntRevUnsigned();
        int zAxisOrientation = fileIn.readIntRevUnsigned();
        int compressed = fileIn.readIntRevUnsigned();

        fileIn.readIntRevUnsigned(); 

        int numMatrices = fileIn.readIntRevUnsigned();


        for (int i = 0; i < numMatrices; i++) {
            // read matrix name
            int nameLength = fileIn.readByteUnsigned();
            String name = fileIn.readASCIIString(nameLength);
            addLayer(name);

            // read matrix size
            int sx = fileIn.readIntRevUnsigned();
            int sy = fileIn.readIntRevUnsigned();
            int sz = fileIn.readIntRevUnsigned();

            // read offset size
            int cx = fileIn.readIntRev();
            int cy = fileIn.readIntRev();
            int cz = fileIn.readIntRev();

            ByteBuffer byteBuffer = ByteBuffer.allocate(4);

            if (compressed == 0) { // uncompressed
                for(int z = 0; z < sz; z++) {
                    for(int y = 0; y < sy; y++) {
                        for(int x = 0; x < sx; x++) {
                            int c1 = fileIn.readByteUnsigned();
                            int c2 = fileIn.readByteUnsigned();
                            int c3 = fileIn.readByteUnsigned();
                            int a = fileIn.readByteUnsigned(); // read visibility encoding
                            addVoxel(colorFormat, zAxisOrientation, cx, cy, cz, z, x, y, c1, c2, c3, a);
                        }
                    }
                }
            } else { // compressed

                int z = 0;

                while (z < sz) {
                    int index = -1;
                    while (true) {
                        int data = fileIn.readIntRev();
                        if (data == NEXT_SLICE_FLAG) {
                            break;
                        } else if (data == CODE_FLAG) {
                            int count = fileIn.readIntRevUnsigned();
                            data = fileIn.readIntRev();

                            for (int j = 0; j < count; j++) {
                                createVoxel(colorFormat, zAxisOrientation, sx, cx, cy, cz, byteBuffer, z, index, data);
                                index++;
                            }
                        } else {
                            createVoxel(colorFormat, zAxisOrientation, sx, cx, cy, cz, byteBuffer, z, index, data);
                            index++;
                        }
                    }
                    z++;
                }

            }

        }

        return true;
    }

	private void createVoxel(int colorFormat, int zAxisOrientation, int sx, int cx, int cy, int cz,
			ByteBuffer byteBuffer, int z, int index, int data) {
		int x = (index + 1)%sx;
		int y = (index + 1)/sx;
		
		byteBuffer.position(0);
		byteBuffer.putInt(data);
		int c1 = byteBuffer.get(3) & 0x0000FF;
		int c2 = byteBuffer.get(2) & 0x0000FF;
		int c3 = byteBuffer.get(1) & 0x0000FF;
		int a = byteBuffer.get(0) & 0x0000FF; // read visibility encoding
		addVoxel(colorFormat, zAxisOrientation, cx, cy, cz, z, x, y, c1, c2, c3, a);
	}

	private void addVoxel(int colorFormat, int zAxisOrientation, int cx, int cy, int cz, int z, int x, int y, int c1,
			int c2, int c3, int a) {
		if (a != 0) { // if voxel is not invisible
		    int rgb = colorFormat == 0 ? new Color(c1,c2,c3).getRGB() : new Color(c3, c2, c1).getRGB();
		    if (zAxisOrientation == 1) {
		        addVoxel(x + cx, -y - cy, z + cz, rgb);
		    } else {
		        addVoxel(z + cz, -y - cy, x + cx, rgb);
		    }
		}
	}
}
