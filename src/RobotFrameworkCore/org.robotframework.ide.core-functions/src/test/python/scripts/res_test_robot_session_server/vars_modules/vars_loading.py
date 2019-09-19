def get_variables(arg=None):
    variables = {}
    with open('vars.txt') as v:
        for line in v:
            (v_name, v_val) = line.split('=', 1)
            variables[v_name.strip()] = v_val.strip()
    return variables
        