import { Menu, Transition } from "@headlessui/react";
import Button from "../ui/Button";
import { LuCalendar, LuChevronDown } from "react-icons/lu";
import { Fragment } from "react/jsx-runtime";
import clsx from "clsx";

export type DateFilterOption = 'today' | 'this_month' | 'this_year' | 'all';

interface DateFilterDropdownProps {
    selectedOption: DateFilterOption;
    onSelect: (option: DateFilterOption) => void;
    options: { key: DateFilterOption; label: string }[];
}

const DateFilterDropdown: React.FC<DateFilterDropdownProps> = ({
    selectedOption, onSelect, options
}) => {
    const selectedLabel = options.find(opt => opt.key === selectedOption)?.label;

    return (
        <Menu as="div" className="relative inline-block text-left">
            <div className="flex rounded-btn shadow-sm">
                <Button 
                    variant="primary" 
                    className="rounded-r-none pl-4 pr-3 flex items-center"
                    onClick={() => onSelect(selectedOption)}
                >
                    <LuCalendar className="mr-2 h-4 w-4" />
                    {selectedLabel}
                </Button>
                <Menu.Button as={Button} variant="primary" className="rounded-l-none px-2">
                    <LuChevronDown className="h-5 w-5" aria-hidden="true" />
                </Menu.Button>
            </div>

            <Transition
                as={Fragment}
                enter="transition ease-out duration-100"
                enterFrom="transform opacity-0 scale-95"
                enterTo="transform opacity-100 scale-100"
                leave="transition ease-in duration-75"
                leaveFrom="transform opacity-100 scale-100"
                leaveTo="transform opcacity-0 scale-95">
                <Menu.Items className="absolute left-0 mt-2 w-56 origin-top-left divide-y divide-gray-100 dark:divide-gray-700 rounded-card bg-card-light dark:bg-card-dark shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none">
                    <div className="px-1 py-1">
                        {options.map((option) => (
                            <Menu.Item key={option.key}>
                                {({ active }) => (
                                    <button
                                        onClick={() => onSelect(option.key)}
                                        className={clsx('group flex w-full items-center rounded-md px-2 py-2 text-sm', active ? 'bg-brand-primary text-white' : 'text-text-primary dark:text-gray-200')}
                                    >
                                        {option.label}
                                    </button>
                                )}
                            </Menu.Item>
                        ))}
                    </div>
                </Menu.Items>
            </Transition>
        </Menu>
    );
}
export default DateFilterDropdown;