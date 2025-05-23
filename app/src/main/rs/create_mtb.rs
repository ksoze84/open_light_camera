#pragma version(1)
#pragma rs java_package_name(net.sourceforge.openlightcamera)
#pragma rs_fp_relaxed

rs_allocation out_bitmap; // the resultant median threshold bitmap

int median_value = 0;
int start_x = 0, start_y = 0;

void __attribute__((kernel)) create_mtb(uchar4 in, uint32_t x, uint32_t y) {
    uchar value = max(in.r, in.g);
    value = max(value, in.b);

    uchar out;
    // take care of being unsigned!
    // ignore small differences to reduce effect of noise - this helps testHDR22
    int diff;
    if( value > median_value )
        diff = value - median_value;
    else
        diff = median_value - value;
    if( diff <= 4 ) // should be same value as min_diff_c in HDRProcessor.autoAlignment()
        out = 127;
    else if( value <= median_value )
        out = 0;
    else
        out = 255;
    rsSetElementAt_uchar(out_bitmap, out, x - start_x, y - start_y);
    //return out;
}

void __attribute__((kernel)) create_greyscale(uchar4 in, uint32_t x, uint32_t y) {
    uchar value = max(in.r, in.g);
    value = max(value, in.b);

    rsSetElementAt_uchar(out_bitmap, value, x - start_x, y - start_y);
}

void __attribute__((kernel)) create_greyscale_f(float3 in_f, uint32_t x, uint32_t y) {
    uchar3 in;
    in.r = (uchar)clamp(in_f.r+0.5f, 0.0f, 255.0f);
    in.g = (uchar)clamp(in_f.g+0.5f, 0.0f, 255.0f);
    in.b = (uchar)clamp(in_f.b+0.5f, 0.0f, 255.0f);

    uchar value = max(in.r, in.g);
    value = max(value, in.b);

    rsSetElementAt_uchar(out_bitmap, value, x - start_x, y - start_y);
}
