import React, { useState } from 'react';
import { Link } from 'react-router-dom';

import "./css/createquestion.css"

const CreateQuestionForm = ({productId}) => {
    const [content, setContent] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const handleSubmit = async (event) => {
        if (localStorage.getItem("user") != null) {
            event.preventDefault();

            const user = JSON.parse(localStorage.getItem("user"));

            const user_id = user.userId;

            const questionData = {
                userId: parseInt(user_id, 10),
                productId: parseInt(productId, 10),
                content
            };

            try {
                const response = await fetch('http://localhost:8080/create/question', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(questionData)
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.error || 'An error occurred');
                }

                const responseData = await response.json();
                setSuccess('Yorumunuz başarılı bir şekilde oluşturuldu.');
                setError('');
                console.log(responseData);
            } catch (error) {
                setError(error.message);
                setSuccess('');
            }
        }
    };

    return (
        <div>
            {
                localStorage.getItem("user") != null?
                    <div className='ask_a_question'>
                        <form onSubmit={handleSubmit}>
                            <div className='ask_a_question_flex_container'>
                                <label className='ask_a_question_title'>
                                    Bir Soru Sorun
                                </label>
                                <textarea
                                    id="content"
                                    value={content}
                                    onChange={(e) => setContent(e.target.value)}
                                    required
                                />
                            </div>
                            <button type="submit">Soru'yu gönder</button>
                        </form>
                    </div>
                :
                <div className='login_before_asking'>
                    Soru sormak için lütfen <Link to={"/login"}>giriş yapın.</Link>
                </div>
            }
            {error && <p style={{ color: 'red' }}>{error}</p>}
            {success && <p style={{ color: 'green' }}>{success}</p>}
        </div>
    );
};

export default CreateQuestionForm;