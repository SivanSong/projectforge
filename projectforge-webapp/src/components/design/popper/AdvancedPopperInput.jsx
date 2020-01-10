import classNames from 'classnames';
import PropTypes from 'prop-types';
import React from 'react';
import { Input } from '../index';
import styles from './Popper.module.scss';

function AdvancedPopperInput(
    {
        dark,
        // Extract 'dispatch' so it's not passed to the input tag
        dispatch,
        forwardRef,
        icon,
        onCancel,
        onKeyDown,
        ...props
    },
) {
    const handleKeyDown = (event) => {
        if (onCancel && event.key === 'Escape') {
            onCancel();
        }

        if (onKeyDown) {
            onKeyDown(event);
        }
    };

    return (
        <Input
            ref={forwardRef}
            icon={icon}
            className={classNames(styles.input, { [styles.dark]: dark })}
            autoComplete="off"
            {...props}
            onKeyDown={handleKeyDown}
        />
    );
}

AdvancedPopperInput.propTypes = {
    id: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    dark: PropTypes.bool,
    dispatch: PropTypes.func,
    forwardRef: PropTypes.shape({}),
    icon: PropTypes.shape({}),
    onCancel: PropTypes.func,
    onKeyDown: PropTypes.func,
    placeholder: PropTypes.string,
    value: PropTypes.string,
};

AdvancedPopperInput.defaultProps = {
    dark: false,
    dispatch: undefined,
    forwardRef: undefined,
    icon: undefined,
    onCancel: undefined,
    onKeyDown: undefined,
    placeholder: '',
    value: '',
};

export default AdvancedPopperInput;