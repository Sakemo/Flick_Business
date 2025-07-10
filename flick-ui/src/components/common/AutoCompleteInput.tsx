import { useEffect, useRef, useState } from 'react';
import Input from '../ui/Input';
import { LuSearch, LuX } from 'react-icons/lu';
import Button from '../ui/Button';

interface AutoCompleteOption {
  value: string | number;
  label: string;
}

interface AutoCompleteInputProps {
  label?: string;
  placeholder?: string;
  options: { value: number; label: string }[];
  value?: AutoCompleteOption | null;
  onChange: (selectedOption: AutoCompleteOption | null) => void;
  onInputChange?: (inputValue: string) => void;
  isLoading?: boolean;
  disabled?: boolean;
  error?: string;
}

const AutoCompleteInput: React.FC<AutoCompleteInputProps> = ({
  label,
  placeholder,
  options,
  value,
  onChange,
  onInputChange,
  isLoading,
  disabled,
  error,
}) => {
  const [inputValue, setInputValue] = useState<string>(value?.label || '');
  const [showSuggestions, setShowSuggestions] = useState<boolean>(false);
  const [filterdOptions, setFilteredOptions] = useState<AutoCompleteOption[]>([]);
  const wrapperRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (inputValue) {
      setFilteredOptions(
        options.filter((option) => option.label.toLowerCase().includes(inputValue.toLowerCase()))
      );
    } else {
      setFilteredOptions([]);
    }
  }, [inputValue, options]);

  useEffect(() => {
    setInputValue(value?.label || '');
  }, [value]);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target as Node)) {
        setShowSuggestions(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [wrapperRef]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newInputValue = e.target.value;
    setInputValue(newInputValue);
    setShowSuggestions(true);
    if (onInputChange) {
      onInputChange(newInputValue);
    }
    if (newInputValue === '') {
      onChange(null);
    }
  };

  const handleSelectOption = (option: AutoCompleteOption) => {
    setInputValue(option.label);
    onChange(option);
    setShowSuggestions(false);
  };

  const clearSelection = () => {
    setInputValue('');
    onChange(null);
    setShowSuggestions(false);
  };

  return (
    <div className="relative w-full" ref={wrapperRef}>
      <Input
        label={label}
        placeholder={placeholder}
        value={inputValue}
        onChange={handleInputChange}
        onFocus={() => setShowSuggestions(true)}
        iconLeft={<LuSearch />}
        disabled={disabled}
        error={error}
        autoComplete="off"
        className={inputValue && !value ? 'pr-10' : ''}
      />
      {inputValue && (
        <Button
          type="button"
          variant="ghost"
          size="icon"
          onClick={clearSelection}
          className="absolute right-1 top-1/2 transform - translate-y-1/2 mt-3 h-8 w-8 p-1 text-gray-400 hover:text-gray-600"
          title="Limpar Seleção"
        >
          <LuX className="h-4 w-4" />
        </Button>
      )}

      {showSuggestions && inputValue && setFilteredOptions.length > 0 && (
        <ul className="absolute z-10 w-full bg-card-light dark:bg-card-dark border border-gray-300 dark:border-grat-600 rounded-md shadow-lg max-h-60 overflow-y-auto mt-1">
          {filterdOptions.map((option) => (
            <li
              key={option.value}
              className="px-3 py-2 hover:bg-brand-muted/50 dark:hover:bg-gray-700 cursor-pointer text-sm text-text-primary dark:text-gray-200"
              onClick={() => handleSelectOption(option)}
              onMouseDown={(e) => e.preventDefault()}
            >
              {option.label}
            </li>
          ))}
        </ul>
      )}
      {showSuggestions && inputValue && filterdOptions.length === 0 && !isLoading && (
        <div className="absolute z-10 w-full bg-card-light dark:bg-card-dark border border-gray-300 dark:border-gray-600 rounded-md shadow-lg p-3 mt-1 text-sm text-text-secondary">
          Nenhum produto encontrado
        </div>
      )}
    </div>
  );
};
export default AutoCompleteInput;
