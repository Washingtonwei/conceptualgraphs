package charger.util;

import java.awt.image.RGBImageFilter;

/**
 * Inverts colors according to the color wheel; e.g. blue becomes yellow, etc.
 * Trying not to use this, since it seems to be really expensive in time and space.
 */
public class InvertFilter extends RGBImageFilter {

    public InvertFilter() {
        // The filter's operation does not depend on the
        // pixel's location, so IndexColorModels can be
        // filtered directly.
//        canFilterIndexColorModel = true;
    }

    public int filterRGBold( int x, int y, int rgb ) {
        return ( ( rgb & 0xff00ff00 )
                | ( ( rgb & 0xff0000 ) >> 16 )
                | ( ( rgb & 0xff ) << 16 ) );
    }

    public int filterRGB( int x, int y, int rgb ) {
        if ( rgb == 0xff000000 ) {
            return 0xffffffff;
        }
        if ( rgb == 0xffffffff ) {
            return 0xff000000;
        }
        return ( rgb * -1 ) | 0xff000000;	// invert all the bits, leaving mask alone
    }
}
