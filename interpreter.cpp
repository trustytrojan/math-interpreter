#include <iostream>
#include <map>
#include <regex>
#include <vector>
using namespace std;

const string
  int_ = "(-?[0-9]+)",
  var_ = "([a-zA-Z]+[a-zA-Z0-9_]*)",
  iv = '('+int_+'|'+var_+')',
  addition_ = "(("+iv+"\\+)+"+iv+')',
  subtraction_ = "(("+iv+"-)+"+iv+')',
  a_or_s = "(("+addition_+")|("+subtraction_+"))+"
;

const regex
  integer(int_),
  variable(var_),
  assignment(var_+"=.+"),
  addition(addition_),
  subtraction(subtraction_),
  add_or_sub(a_or_s)
;

map<string,int> integers;

void trim_whitespace(string &s) {
  for(string::const_iterator itr = s.begin(); itr != s.end(); itr++)
    if(isspace(*itr))
      s.erase(itr);
}

int vector_int_sum(const vector<int> &v) {
  int sum = 0;
  for(const int i : v)
    sum += i;
  return sum;
}

vector<string> string_split(const string &input, const char c) {
  vector<string> v;
  string s;
  for(unsigned i = 0; i < input.length(); i++)
    if(input[i] == c) { v.push_back(s); s.clear(); }
    else s += input[i];
  v.push_back(s);
  return v;
}

int get_integer(const string &s) {
  const map<string,int>::const_iterator itr = integers.find(s);
  if(itr == integers.end())
    throw "error: "+s+" is undefined";
  return itr->second;
}

int eval_int_var(const string &r) {
  if(regex_match(r, integer)) return stoi(r);
  if(regex_match(r, variable)) return get_integer(r);
  throw string("syntax error from eval_int_var");
}

int integer_addition(const string &input) {
  vector<int> vi;
  for(const string &s : string_split(input, '+'))
    vi.push_back(eval_int_var(s));
  return vector_int_sum(vi);
}

int integer_subtraction(const string &input) {
  vector<int> vi;
  for(const string &s : string_split(input, '-'))
    vi.push_back(eval_int_var(s));
  for(unsigned i = 1; i < vi.size(); i++)
    vi[i] = -vi[i];
  return vector_int_sum(vi);
}

int integer_add_or_sub(const string &input) {
  
}

int eval_no_assign(const string &r) {
  if(regex_match(r, integer)) return stoi(r);
  if(regex_match(r, variable)) return get_integer(r);
  if(regex_match(r, addition)) return integer_addition(r);
  if(regex_match(r, subtraction)) return integer_subtraction(r);
  throw string("syntax error from eval_no_assign");
}

int assign_integer(const string &input) {
  vector<string> v(string_split(input, '='));
  const int rhs = eval_no_assign(v[1]);
  if(integers.find(v[0]) != integers.end())
    integers.find(v[0])->second = rhs;
  else integers.insert_or_assign(v[0], rhs);
  return rhs;
}

int evaluate(const string &r) {
  if(regex_match(r, integer)) return stoi(r);
  if(regex_match(r, variable)) return get_integer(r);
  if(regex_match(r, assignment)) return assign_integer(r);
  if(regex_match(r, addition)) return integer_addition(r);
  if(regex_match(r, subtraction)) return integer_subtraction(r);
  throw string("syntax error");
}

int main() {
  while(1) {
    cout << "> ";
    string input;
    {
      char x[500];
      cin.getline(x, 500);
      input.assign(x);
    }
    trim_whitespace(input);
    try { cout << evaluate(input); }
    catch(string err) { cout << err; }
    cout << '\n';
  }
}