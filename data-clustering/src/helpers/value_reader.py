import json
import pandas as pd

from datetime import datetime
from typing import Any, Callable
from enum import Enum

DATE_PARSER_ARGO = "%Y-%m-%dT%H:%M:%SZ"
DATE_PARSER_DB = "%Y-%m-%d %H:%M:%S.%f %z"
DATE_PARSER_DB_2 = "%Y-%m-%d %H:%M:%S.%f"


def FORMATTER(s): return s.apply('{0:.0f}'.format)
def FORMATTER_CORR(s): return s.apply('{0:.3f}'.format)


class DateParser(Enum):
    def PARSER_ARGO(
        date_string) -> datetime: return datetime.strptime(date_string, DATE_PARSER_ARGO)
    def PARSER_DATABASE(
        date_string) -> datetime: parse_date(date_string)

def parse_date(date_string: str):
    try:
        return datetime.strptime(date_string, DATE_PARSER_DB)
    except ValueError as v:
        ulr = len(v.args[0].partition('unconverted data remains: ')[2])
        if ulr:
            return datetime.strptime(date_string[:-ulr], date_string)
        else:
            return datetime.strptime(date_string, DATE_PARSER_DB_2)

def read_value_or_return_default(key: str, object: dict | None, default: Any = 'undefined') -> Any:
    '''
    Method returns value for a given key or returns default value when key or key value is not present.

    Parameters:
    key - key for which the value is to be returned
    object - object from which the value is to be returned
    default - default value that is to be returned (if not specified then 'undefined' string is returned)

    Returns: value for selected key
    '''
    if not object:
        return default

    return object[key] if key in object and (type(object[key]) == list or not pd.isna(object[key])) else default


def read_date_value_or_return_default(key: str, date_object: dict | None, parser: DateParser = DateParser.PARSER_DATABASE) -> datetime:
    '''
    Method returns date object parsed for a given key or returns None when key or key value is not present.

    Parameters:
    key - key for which the date string is to be returned
    object - object from which the date string is to be returned
    parser - parser used to convert date string into datetime object

    Returns: datetime value for selected key
    '''
    date_string = read_value_or_return_default(key, date_object, None)
    return parser(date_string) if date_string else None


def read_json_or_return_default(object_to_read: str | None) -> object:
    '''
    Method returns parsed json object or returns None when object is None.

    Parameters:
    object_to_read - string that is to be parsed to object

    Returns: parsed json object or None
    '''
    return json.loads(object_to_read) if object_to_read else None


def read_first_or_return_default(list_to_traverse: list, predicate: Callable, default: Any = None) -> datetime:
    '''
    Method returns first value from the list that matches given predicate or returns default value otherwise

    Parameters:
    list_to_traverse - list that is to be traversed
    predicate - function used to evaluate list elements
    default - default value that ia to be returned (by default none)

    Returns: first element that matches predicate or default value
    '''
    return next(filter(predicate, list_to_traverse), default)
