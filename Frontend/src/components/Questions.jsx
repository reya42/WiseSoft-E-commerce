import React, { useEffect, useState } from 'react'

import Question from './Question';

const Questions = ({productLink}) => {

    const [questions, setQuestions] = useState();
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);

    useEffect(()=>{
        const fetchQuestions = async () => {
            try {
                setLoading(true);
                const response = await fetch(`http://localhost:8080/questions/${productLink}?size=3`);
                const data = await response.json();

                if (!response.ok) {
                    if (data.error != null) {
                        setError(data.error);
                        throw new Error(data.error);
                    }
                    else if (data.message != null) {
                        setError(data.message);
                        throw new Error(data.message);
                    }
                }
                else
                {
                    setError("");
                    setQuestions(data);
                    setLoading(false);
                    return data;
                }
            } catch (error) {
                console.error(error.message);
            }
        };
        fetchQuestions();
    },[])

    if (error != "") {
        <div className="single_product_questions_container">
            <div className="single_questions_title">
                Ürünle İlgili Sorular
            </div>
            {error}
        </div>
    }
    
    return (
        <div className="single_product_questions_container">
            <div className="single_questions_title">
                Ürünle İlgili Sorular
            </div>
            {
                loading ?
                    "Loading..."
                :
                <div className="">
                    <div className="single_product_question_buttons">
                        {
                            !questions.last &&  
                                <a className="single_product_question_button" href={`http://localhost:3000/products/${productLink}/questions`}>
                                    Tümünü görüntüle
                                </a>
                        }
                        <a className="single_product_question_button" href={`http://localhost:3000/products/${productLink}/questions`}>
                            Bir Soru Sor
                        </a>
                    </div>
                    {
                        questions.content && 
                        (
                            questions.content.length != 0 ?
                            <div className="single_product_questions">
                                {questions.content.map((item, index) => (
                                    <Question data={item} singleProduct={true}/>
                                ))}
                            </div>
                            :
                            <div className="single_product_no_questions">
                                Bu ürünle ilgili hiçbir soru bulunamadı. Bir soru sorarak başlayın...
                            </div>
                        )
                    }
                </div>
            }
        </div>
    )
}

export default Questions