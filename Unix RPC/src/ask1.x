struct int_vector{
    int data<>;
};

struct minmax_result{
    int min;
    int max;
};

struct scalar_vector_input{
    float a;
    int_vector vec;
};

struct float_vector{
    float data<>;
};

program CALC_PROG{
    version CALC_VERS{
        float MEAN(int_vector)= 1;
        minmax_result MINMAX(int_vector)= 2;
        float_vector MULTIPLY(scalar_vector_input)= 3;
    }= 1;
}= 0x164E882;