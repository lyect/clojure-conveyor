import pathlib
import re


class ExceptionUsage:

    def __init__(self, type, cause):
        self.type = type
        self.cause = cause

    def __eq__(self, other):
        if not isinstance(other, ExceptionUsage):
            return False
        return self.type  == other.type and \
               self.cause == other.cause
    
    def __ne__(self, other):
        return not self == other

    def __hash__(self):
        return hash((self.type, self.cause))
    
    def __repr__(self):
        return f"BlockException({self.type}, {self.cause})"

    def __str__(self):
        return self.__repr__()


class PartExceptionUsageValidator:

    def __init__(self, part_path, part_name):
        self.__part_path = pathlib.Path(part_path)
        self.__part_name = part_name
        self.__src_dir_path = pathlib.Path(__file__).resolve().parents[1] / "src"
        self.__part_dir_path = self.__src_dir_path / self.__part_path
        self.__exceptions_file_path = self.__part_dir_path / "exceptions.clj"

        self.__exceptions_file = open(self.__exceptions_file_path, "r")
        self.__exceptions_file_str = self.__exceptions_file.read()
        self.__exceptions_file.close()
        self.__declared_exception_types = set()
        self.__declared_exception_causes = set()
        self.__exception_types_list = list()
        self.__exception_causes_list = list()
        self.__type_cause_correspondence = dict()
        self.__exception_usages = set()
        self.__parse_declared_exception_types()
        self.__parse_declared_exception_causes()
        self.__parse_exception_types_list()
        self.__parse_exception_causes_list()
        self.__parse_type_cause_correspondence()
        self.__parse_exception_usages()

    def __parse_declared_exception_types(self):
        declared_exception_types_re = r"\(def\s+[a-z\-]+\s+::type[a-z\-]+\)"
        declared_exception_types = re.findall(declared_exception_types_re, self.__exceptions_file_str)
        declared_exception_types = map(lambda x: x[5:],         declared_exception_types)
        declared_exception_types = map(lambda x: x.split("::"), declared_exception_types)
        declared_exception_types = map(lambda x: x[0],          declared_exception_types)
        declared_exception_types = map(lambda x: x.strip(),     declared_exception_types)
        for declared_exception_type in declared_exception_types:
            if declared_exception_type in self.__declared_exception_types:
                raise RuntimeError(f"Type \"{declared_exception_type}\" for part \"{self.__part_name}\" is already declared")
            self.__declared_exception_types.add(declared_exception_type)

    def __parse_declared_exception_causes(self):
        declared_exception_causes_re = r"\(def\s+[a-z\-]+\s+::cause[a-z\-]+\)"
        declared_exception_causes = re.findall(declared_exception_causes_re, self.__exceptions_file_str)
        declared_exception_causes = map(lambda x: x[5:],         declared_exception_causes)
        declared_exception_causes = map(lambda x: x.split("::"), declared_exception_causes)
        declared_exception_causes = map(lambda x: x[0],          declared_exception_causes)
        declared_exception_causes = map(lambda x: x.strip(),     declared_exception_causes)
        for declared_exception_cause in declared_exception_causes:
            if declared_exception_cause in self.__declared_exception_causes:
                raise RuntimeError(f"Cause \"{declared_exception_cause}\" for part \"{self.__part_name}\" is already declared")
            self.__declared_exception_causes.add(declared_exception_cause)

    def __parse_exception_types_list(self):
        exception_types_list_re = r"\(def\s+\^:private\s+type-list\s+\[[a-z\-\s]+\]\)"
        exception_types_list = re.findall(exception_types_list_re, self.__exceptions_file_str)
        if len(exception_types_list) > 1:
            raise RuntimeError(f"Too many ({len(exception_types_list)}) exception types lists are defined for part \"{self.__part_name}\"")
        exception_types_list = exception_types_list[0]
        exception_types_list = re.compile(r"\s+").sub(" ", exception_types_list).strip()
        exception_types_list = exception_types_list[len("(def ^:private type-list ["):][:-2]
        self.__exception_types_list = exception_types_list.split()

    def __parse_exception_causes_list(self):
        exception_causes_list_re = r"\(def\s+\^:private\s+cause-list\s+\[[a-z\-\s]+\]\)"
        exception_causes_list = re.findall(exception_causes_list_re, self.__exceptions_file_str)
        if len(exception_causes_list) > 1:
            raise RuntimeError(f"Too many ({len(exception_causes_list)}) exception causes lists are defined for part \"{self.__part_name}\"")
        exception_causes_list = exception_causes_list[0]
        exception_causes_list = re.compile(r"\s+").sub(" ", exception_causes_list).strip()
        exception_causes_list = exception_causes_list[len("(def ^:private cause-list ["):][:-2]
        self.__exception_causes_list = exception_causes_list.split()

    def __parse_type_cause_correspondence(self):
        type_cause_correspondence_re = r"\(def\s+\^:private types-causes-correspondence\s+\{[a-z\-\[\]\s]+\}\)"
        type_cause_correspondence = re.findall(type_cause_correspondence_re, self.__exceptions_file_str)
        if len(type_cause_correspondence) > 1:
            raise RuntimeError(f"Too many ({len(type_cause_correspondence)}) correspondences are defined for part \"{self.__part_name}\"")
        type_cause_correspondence = type_cause_correspondence[0]
        type_cause_correspondence = re.compile(r"\s+").sub(" ", type_cause_correspondence)
        type_cause_correspondence = type_cause_correspondence[len("(def ^:private types-causes-correspondence {"):][:-2]
        type_cause_correspondence = type_cause_correspondence.replace("[", "")
        type_cause_correspondence = type_cause_correspondence.split("]")[:-1]
        type_cause_correspondence = map(lambda x: x.split(), type_cause_correspondence)
        
        type_cause_correspondence = map(lambda x: (x[0].strip(), (_x.strip() for _x in x[1:])), type_cause_correspondence)
        for type, causes in type_cause_correspondence:
            for cause in causes:
                if type not in self.__type_cause_correspondence:
                    self.__type_cause_correspondence[type] = list()
                self.__type_cause_correspondence[type].append(cause)

    def __parse_exception_usages(self):
        exception_usage_re = fr"{self.__part_name}-exceptions\/construct\s+{self.__part_name}-exceptions\/[a-z\-]+\s+{self.__part_name}-exceptions\/[a-z\-]+\s"
        for file_path in self.__part_dir_path.rglob('*.clj'):
            relative_file_path = file_path.relative_to(self.__part_dir_path)
            if relative_file_path == "exceptions.clj":
                continue
            with open(file_path) as f:
                exception_usages = re.findall(exception_usage_re, f.read())
                exception_usages = map(lambda x: re.compile(r"\s+").sub(" ", x), exception_usages)
                exception_usages = map(lambda x: x.strip(), exception_usages)
                exception_usages = map(lambda x: x.split(), exception_usages)
                exception_usages = map(lambda x: (x[1].replace(f"{self.__part_name}-exceptions/", ""), x[2].replace(f"{self.__part_name}-exceptions/", "")), exception_usages)
                exception_usages = map(lambda x: ExceptionUsage(x[0], x[1]), exception_usages)
                self.__exception_usages.update(exception_usages)

    def validate(self):
        for exception_type in self.__exception_types_list:
            if exception_type not in self.__declared_exception_types:
                print(f"Undeclared type \"{exception_type}\" in types list for part \"{self.__part_name}\"")
                return False
            
        if len(self.__declared_exception_types.difference(set(self.__exception_types_list))) != 0:
            print(f"Some declared types are not in list for part \"{self.__part_name}\"")
            return False
        
        for exception_cause in self.__exception_causes_list:
            if exception_cause not in self.__declared_exception_causes:
                print(f"Undeclared cause \"{exception_cause}\" in causes list for part \"{self.__part_name}\"")
                return False
            
        if len(self.__declared_exception_causes.difference(set(self.__exception_causes_list))) != 0:
            print(f"Some declared causes are not in list for part \"{self.__part_name}\"")
            return False
                
        for exception_usage in self.__exception_usages:
            if exception_usage.type not in self.__declared_exception_types:
                print(f"Undeclared type \"{exception_usage.type}\" in exception usage for part \"{self.__part_name}\"")
                return False
            if exception_usage.cause not in self.__declared_exception_causes:
                print(f"Undeclared cause \"{exception_usage.cause}\" in exception usage for part \"{self.__part_name}\"")
                return False
        
        type_cause_correspondence_set = set()
        for type, causes in self.__type_cause_correspondence.items():
            if type not in self.__declared_exception_types:
                print(f"Undeclared type \"{type}\" in correspondence for part \"{self.__part_name}\"")
                return False
            if len(set(causes)) < len(causes):
                print(f"Causes repeated for type \"{type}\" for part \"{self.__part_name}\"")
                return False
            for cause in causes:
                if cause not in self.__declared_exception_causes:
                    print(f"Undeclared cause \"{cause}\" in correspondence for part \"{self.__part_name}\"")
                    return False
                type_cause_correspondence_set.add(ExceptionUsage(type, cause))

        if len(type_cause_correspondence_set.difference(self.__exception_usages)) > 0:
            print(f"Not all correspondences are used for part \"{self.__part_name}\"")
            for x in type_cause_correspondence_set.difference(self.__exception_usages):
                print(x)
            return False
        if len(self.__exception_usages.difference(type_cause_correspondence_set)) > 0:
            print(f"Used not from correspondence for part \"{self.__part_name}\"")
            return False
        
        return True

def validate_part(part_path, part_name):
    try:
        validator = PartExceptionUsageValidator(part_path, part_name)
    except RuntimeError as e:
        print(f"Part \"{part_name}\" by path \"{part_path}\" invalidated:\n{e}")
    if validator.validate():
        print(f"Part \"{part_name}\" by path \"{part_path}\" validated")
    else:
        print(f"Part \"{part_name}\" by path \"{part_path}\" invalidated")

def main():
    validate_part("blocks/channel", "channel")
    validate_part("blocks/edge",    "edge")
    validate_part("blocks/node",    "node")
    validate_part("blocks/vertex",  "vertex")
    validate_part("conveyor",       "conveyor")

main()